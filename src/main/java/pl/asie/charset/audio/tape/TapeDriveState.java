/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.audio.tape;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import pl.asie.charset.api.audio.IAudioSink;
import pl.asie.charset.api.audio.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.audio.*;
import pl.asie.charset.lib.inventory.InventorySimple;

public class TapeDriveState implements ITickable, INBTSerializable<NBTTagCompound> {
	protected int counter;
	private final PartTapeDrive owner;
	private final AudioSinkPart internalSpeaker;
	private final InventorySimple inventory;
	private State state = State.STOPPED, lastState;
	private Integer sourceId;

	public TapeDriveState(PartTapeDrive owner, InventorySimple inventory) {
		this.owner = owner;
		this.internalSpeaker = new AudioSinkPart(this.owner);
		this.inventory = inventory;
	}

	public void setState(State state) {
		this.lastState = this.state;
		this.state = state;
	}

	public int getCounter() {
		return counter;
	}

	public void resetCounter() {
		counter = 0;
	}

	@Override
	public void update() {
		int lastCounter = counter;

		if (state != State.STOPPED) {
			if (sourceId == null) {
				sourceId = AudioUtils.start();
			}

			boolean found = false;
			ItemStack stack = inventory.getStackInSlot(0);
			if (stack != null && stack.hasCapability(ModCharsetAudio.CAP_STORAGE, null)) {
				IDataStorage storage = stack.getCapability(ModCharsetAudio.CAP_STORAGE, null);
				if (storage != null) {
					found = true;
					int rel = storage.getPosition();

					if (state == State.PLAYING) {
						byte[] data = new byte[300];
						int len = storage.read(data, false);

						AudioPacketDFPWM packetDFPWM = new AudioPacketDFPWM(sourceId, data, 1);
						packetDFPWM.beginPropagation();

						World world = owner.getWorld();
						BlockPos pos = owner.getPos();
						for (EnumFacing facing : EnumFacing.VALUES) {
							TileEntity tile = world.getTileEntity(pos.offset(facing));
							if (tile != null && tile.hasCapability(Capabilities.AUDIO_SINK, facing.getOpposite())) {
								tile.getCapability(Capabilities.AUDIO_SINK, facing.getOpposite()).receive(packetDFPWM);
							}
						}

						if (packetDFPWM.sinkCount() == 0) {
							internalSpeaker.receive(packetDFPWM);
						}

						packetDFPWM.endPropagation();

						if (len < data.length) {
							setState(State.STOPPED);
						}
					} else {
						int offset = 3072 * (state == State.FORWARDING ? 1 : -1);
						int len = storage.seek(offset);
						if (len != offset) {
							setState(State.STOPPED);
						}
					}

					counter += (storage.getPosition() - rel);
				}
			}

			if (!found) {
				setState(State.STOPPED);
			}
		}

		if (lastState != state) {
			ModCharsetAudio.packet.sendToWatching(new PacketDriveState(owner, state), owner);
			if (state == State.STOPPED && lastState == State.PLAYING && sourceId != null) {
				AudioUtils.stop(sourceId);
				sourceId = null;
			}
		}

		if (lastCounter != counter) {
			updateCounter();
		}

		lastState = state;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("st", (byte) state.ordinal());
		compound.setInteger("ct", counter);
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		state = State.STOPPED;

		if (nbt != null) {
			if (nbt.hasKey("st")) {
				int stateId = nbt.getByte("st");
				if (stateId >= 0 && stateId < State.values().length) {
					state = State.values()[stateId];
				}
			}

			counter = nbt.getInteger("ct");
		}
	}

	public State getState() {
		return state;
	}

	public IDataStorage getStorage() {
		ItemStack stack = inventory.getStackInSlot(0);
		if (stack != null && stack.hasCapability(ModCharsetAudio.CAP_STORAGE, null)) {
			IDataStorage storage = stack.getCapability(ModCharsetAudio.CAP_STORAGE, null);
			if (storage != null) {
				return storage;
			}
		}

		return null;
	}

	public void updateCounter() {
		PacketDriveCounter packetDriveCounter = new PacketDriveCounter(owner, counter);
		for (EntityPlayer player : inventory.watchers) {
			ModCharsetAudio.packet.sendTo(packetDriveCounter, (EntityPlayerMP) player);
		}
	}
}
