/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.audio.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import net.minecraftforge.items.ItemStackHandler;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.IAudioDataPCM;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.lib.audio.*;
import pl.asie.charset.lib.audio.codec.DFPWM;
import pl.asie.charset.lib.audio.types.AudioDataDFPWM;
import pl.asie.charset.lib.audio.types.AudioSinkBlock;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.module.audio.util.AudioResampler;

import javax.annotation.Nonnull;
import java.util.Random;

public class RecordPlayerState implements ITickable, IAudioReceiver, INBTSerializable<NBTTagCompound> {
	public enum State {
		STOPPED,
		PLAYING,
		RECORDING
	}

	private final World world;
	private final BlockPos pos;
	private final ItemStackHandler inventory;
	private State state = State.STOPPED, lastState;
	private Integer sourceId;

	private DFPWM recordDFPWM;
	private AudioPacket receivedPacket;
	private int receivedPacketPos;

	public RecordPlayerState(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
		this.inventory = new ItemStackHandler(1) {
			@Override
			public int getSlotLimit(int slot) {
				return 1;
			}

			@Override
			public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
				if (stack.isEmpty() || !stack.hasCapability(CharsetAudioStorage.DATA_STORAGE, null)) {
					return ItemStack.EMPTY;
				}

				return super.insertItem(slot, stack, simulate);
			}
		};
	}

	public void setState(State state) {
		this.lastState = this.state;
		this.state = state;

		if (state != State.RECORDING) {
			receivedPacket = null;
			recordDFPWM = null;
		}
	}

	private void applyNoise(byte[] data, float noiseThreshold) {
		Random rand = world.rand;

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
		if (state != State.STOPPED) {
			if (sourceId == null) {
				sourceId = AudioUtils.start();
			}

			boolean found = false;
			ItemStack stack = inventory.getStackInSlot(0);
			if (!stack.isEmpty() && stack.hasCapability(CharsetAudioStorage.DATA_STORAGE, null)) {
				IDataStorage storage = stack.getCapability(CharsetAudioStorage.DATA_STORAGE, null);
				if (storage != null) {
					found = true;
					if (state == State.PLAYING) {
						int sampleRate = ItemQuartzDisc.DEFAULT_SAMPLE_RATE;
						byte[] data = new byte[sampleRate / (20 * 8)];
						int len = storage.read(data, false);

						AudioPacket packet = new AudioPacket(new AudioDataDFPWM(data, 50).setSourceId(sourceId), 1.0F);
						boolean received = false;

						for (EnumFacing facing : EnumFacing.VALUES) {
							TileEntity tile = world.getTileEntity(pos.offset(facing));
							if (tile != null && tile.hasCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite())) {
								received |= tile.getCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite()).receive(packet);
							}
						}

						if (!received) {
							new AudioSinkBlock(world, pos).receive(packet);
						}

						packet.send();

						if (len < data.length) {
							setState(State.STOPPED);
						}
					} else if (state == State.RECORDING) {
						byte[] dataOut = new byte[ItemQuartzDisc.DEFAULT_SAMPLE_RATE / (20 * 8)];

						if (receivedPacket != null) {
							AudioData data = receivedPacket.getData();
							if (data instanceof IAudioDataPCM) {
								IAudioDataPCM pcm = (IAudioDataPCM) data;
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

									byte[] preEncodeOutput = AudioResampler.toSigned8(
											targetData, pcm.getSampleSize() * 8, 1, pcm.isSampleBigEndian(),
											pcm.isSampleSigned(), pcm.getSampleRate(), ItemQuartzDisc.DEFAULT_SAMPLE_RATE,
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
					}
				}
			}

			if (!found) {
				setState(State.STOPPED);
			}
		}

		if (lastState != state) {
			// TODO
			// CharsetAudioStorage.packet.sendToWatching(new PacketDriveState(owner, state), owner);
			if (state == State.STOPPED && lastState == State.PLAYING && sourceId != null) {
				AudioUtils.stop(sourceId);
				sourceId = null;
			}
		}

		lastState = state;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("st", (byte) state.ordinal());
		compound.setTag("inv", inventory.serializeNBT());
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		state = State.STOPPED;

		if (nbt != null) {
			if (nbt.hasKey("st", Constants.NBT.TAG_ANY_NUMERIC)) {
				int stateId = nbt.getByte("st");
				if (stateId >= 0 && stateId < State.values().length) {
					state = State.values()[stateId];
				}
			}

			if (nbt.hasKey("inv", Constants.NBT.TAG_COMPOUND)) {
				inventory.deserializeNBT(nbt.getCompoundTag("inv"));
			}
		}
	}

	public State getState() {
		return state;
	}

	public IDataStorage getStorage() {
		ItemStack stack = inventory.getStackInSlot(0);
		if (!stack.isEmpty() && stack.hasCapability(CharsetAudioStorage.DATA_STORAGE, null)) {
			IDataStorage storage = stack.getCapability(CharsetAudioStorage.DATA_STORAGE, null);
			if (storage != null) {
				return storage;
			}
		}

		return null;
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
