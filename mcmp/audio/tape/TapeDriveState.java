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

import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.audio.IDataPCM;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.audio.*;
import pl.asie.charset.lib.audio.codec.DFPWM;
import pl.asie.charset.lib.inventory.InventorySimple;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;
import java.util.Random;

public class TapeDriveState implements ITickable, IAudioReceiver, INBTSerializable<NBTTagCompound> {
	protected int counter;
	private final PartTapeDrive owner;
	private final InventorySimple inventory;
	private State state = State.STOPPED, lastState;
	private Integer sourceId;

	private DFPWM recordDFPWM;
	private AudioPacket receivedPacket;
	private int receivedPacketPos;

	public TapeDriveState(PartTapeDrive owner, InventorySimple inventory) {
		this.owner = owner;
		this.inventory = inventory;
	}

	public void setState(State state) {
		this.lastState = this.state;
		this.state = state;

		if (state != State.RECORDING) {
			receivedPacket = null;
			recordDFPWM = null;
		}
	}

	public int getCounter() {
		return counter;
	}

	public void resetCounter() {
		counter = 0;
	}

	private void applyNoise(byte[] data, float noiseThreshold) {
		Random rand = owner.getWorld().rand;

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < 8; j++) {
				if (rand.nextFloat() <= noiseThreshold) {
					if (rand.nextBoolean()) {
						data[i] |= 1 << j;
					} else {
						data[i] &= ~(1 << j);
					}
				}
			}
		}
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

						AudioPacket packet = new AudioPacket(new AudioDataDFPWM(data, 50).setSourceId(sourceId), 1.0F);
						boolean received = false;

						World world = owner.getWorld();
						BlockPos pos = owner.getPos();
						for (EnumFacing facing : EnumFacing.VALUES) {
							TileEntity tile = world.getTileEntity(pos.offset(facing));
							if (tile != null && tile.hasCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite())) {
								received |= tile.getCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite()).receive(packet);
							}
						}

						if (!received) {
							new AudioSinkBlock(owner.getWorld(), owner.getPos()).receive(packet);
						}

						packet.send();

						if (len < data.length) {
							setState(State.STOPPED);
						}
					} else if (state == State.RECORDING) {
						byte[] dataOut = new byte[ItemTape.DEFAULT_SAMPLE_RATE / (20 * 8)];

						if (receivedPacket != null) {
							AudioData data = receivedPacket.getData();
							if (data instanceof IDataPCM) {
								IDataPCM pcm = (IDataPCM) data;
								byte[] audioData = pcm.getSamplePCMData();
								int perTick = audioData.length * 50 / data.getTime();
								int pos = receivedPacketPos;
								int len = perTick;
								if (pos + len > audioData.length) {
									len = audioData.length - pos;
								}

								if (len > 0) {
									byte[] targetData = new byte[len];
									System.arraycopy(audioData, pos, targetData, 0, len);
									receivedPacketPos += len;

									byte[] preEncodeOutput = TapeResampler.toSigned8(
											targetData, pcm.getSampleSize() * 8, 1, pcm.isSampleBigEndian(),
											pcm.isSampleSigned(), pcm.getSampleRate(), ItemTape.DEFAULT_SAMPLE_RATE,
											false);

									if (preEncodeOutput != null) {
										if (recordDFPWM == null) {
											recordDFPWM = new DFPWM();
										}

										recordDFPWM.compress(dataOut, preEncodeOutput, 0, 0, Math.min(dataOut.length, preEncodeOutput.length / 8));
									}
								} else {
									receivedPacketPos = audioData.length;
								}
							}
						}

						storage.write(dataOut);
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

	@Override
	public boolean receive(AudioPacket packet) {
		if (state == State.RECORDING) {
			receivedPacket = packet;
			receivedPacketPos = 0;
			return true;
		} else {
			return false;
		}
	}
}
