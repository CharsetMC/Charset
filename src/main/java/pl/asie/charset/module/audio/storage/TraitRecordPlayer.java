/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.audio.*;
import pl.asie.charset.api.tape.IDataStorage;
import pl.asie.charset.lib.audio.*;
import pl.asie.charset.lib.audio.codec.DFPWM;
import pl.asie.charset.lib.audio.types.AudioDataDFPWM;
import pl.asie.charset.lib.audio.types.AudioSinkBlock;
import pl.asie.charset.lib.block.Trait;
import pl.asie.charset.lib.capability.Capabilities;

import javax.annotation.Nullable;
import java.util.*;

public class TraitRecordPlayer extends Trait implements IAudioSource, IAudioReceiver {
	public enum State {
		STOPPED,
		PLAYING,
		RECORDING,
		PAUSED
	}

	private final IItemHandler inventory;
	private State state = State.STOPPED, lastState;
	private Integer sourceId;

	private DFPWM recordDFPWM;
	private List<AudioPacket> receivedPacket = new ArrayList<>();

	protected double speedIn;

	public double getSpeed() {
		if (ModCharset.isModuleLoaded("power.mechanical")) {
			return Math.min(speedIn, 9.0D);
		} else {
			return 3.0D;
		}
	}

	public TraitRecordPlayer(IItemHandler handler) {
		this.inventory = handler;
	}

	public boolean exposesCapability(EnumFacing facing) {
		return true;
	}

	public void setState(State state) {
		this.lastState = this.state;
		this.state = state;

		if (state != State.RECORDING) {
			receivedPacket.clear();
			recordDFPWM = null;
		}
	}

	private void applyNoise(World world, byte[] data, float noiseThreshold) {
		Random rand = world.rand;

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < 8; j++) {
				if (rand.nextFloat() <= noiseThreshold) {
					data[i] ^= 1 << j;
				}
			}
		}
	}

	public int getSampleRate() {
		int s = (int) (ItemQuartzDisc.DEFAULT_SAMPLE_RATE * getSpeed() / 480);
		return s * 160;
	}

	public void stopAudioPlayback() {
		if (sourceId != null) {
			AudioUtils.stop(sourceId);
			sourceId = null;
		}
	}

	public void update(World world, BlockPos blockPos) {
		if (state != State.STOPPED && state != State.PAUSED && getSpeed() >= 0.01D) {
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
						int sampleRate = getSampleRate();
						byte[] data = new byte[sampleRate / (20 * 8)];
						int len = storage.read(data, false);

						if (world.isRainingAt(blockPos.up())) {
							applyNoise(world, data, 0.008f);
						}

						AudioPacket packet = new AudioPacket(new AudioDataDFPWM(data, 50).setSourceId(sourceId), 1.0F);
						boolean received = false;

						for (EnumFacing facing : EnumFacing.VALUES) {
							TileEntity tile = world.getTileEntity(blockPos.offset(facing));
							if (tile != null && tile.hasCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite())) {
								received |= tile.getCapability(Capabilities.AUDIO_RECEIVER, facing.getOpposite()).receive(packet);
							}
						}

						if (!received) {
							new AudioSinkBlock(world, blockPos).receive(packet);
						}

						packet.send();

						if (len < data.length) {
							setState(State.PAUSED);
						}
					} else if (state == State.RECORDING) {
						// TODO: This should advance at a constant pace

						int sampleRate = getSampleRate();
						if (!receivedPacket.isEmpty()) {
							int adLen = 0;
							for (AudioPacket packet : receivedPacket) {
								adLen = Math.max(adLen, packet.getData().getTime() * sampleRate / 1000);
							}

							boolean added = false;
							byte[] audioData = new byte[adLen];
							for (AudioPacket packet : receivedPacket) {
								AudioData data = packet.getData();
								if (data instanceof IAudioDataPCM && packet.getVolume() >= 0.01f) {
									IAudioDataPCM pcm = (IAudioDataPCM) data;
									int len = audioData.length * 50 / data.getTime();

									if (len > 0) {
										byte[] preEncodeOutput = AudioResampler.toSigned8(
												pcm.getSamplePCMData(), pcm.getSampleSize() * 8, 1, pcm.isSampleBigEndian(),
												pcm.isSampleSigned(), pcm.getSampleRate(), sampleRate,
												false);

										if (preEncodeOutput != null) {
											added = true;
											if (packet.getVolume() >= 0.995f) {
												// fast path - no byte->float->byte casting
												for (int i = 0; i < Math.min(preEncodeOutput.length, audioData.length); i++) {
													audioData[i] += preEncodeOutput[i];
												}
											} else {
												for (int i = 0; i < Math.min(preEncodeOutput.length, audioData.length); i++) {
													audioData[i] += (byte) Math.round(preEncodeOutput[i] * packet.getVolume());
												}
											}
										}
									}
								}
							}

							if (added) {
								if (recordDFPWM == null) {
									recordDFPWM = new DFPWM();
								}

								byte[] dataOut = new byte[audioData.length / 8];
								recordDFPWM.compress(dataOut, audioData, 0, 0, audioData.length / 8);

								storage.write(dataOut);
							}
						}
					}
				}
			}

			if (!found) {
				setState(State.STOPPED);
			}
		}

		if (lastState != state) {
			TileEntity tileEntity = world.getTileEntity(blockPos);
			CharsetAudioStorage.packet.sendToWatching(new PacketDriveState((TileRecordPlayer) tileEntity, state), tileEntity);
			if ((state == State.STOPPED || state == State.PAUSED) && lastState == State.PLAYING && sourceId != null) {
				stopAudioPlayback();
			}
		}

		lastState = state;
		receivedPacket.clear();
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		if (nbt.hasKey("st", Constants.NBT.TAG_ANY_NUMERIC)) {
			int stateId = nbt.getByte("st");
			if (stateId >= 0 && stateId < State.values().length) {
				state = State.values()[stateId];
			}
		}
	}

	@Override
	public NBTTagCompound writeNBTData(boolean isClient) {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("st", (byte) state.ordinal());
		return compound;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return (capability == Capabilities.AUDIO_RECEIVER || capability == Capabilities.AUDIO_SOURCE) && exposesCapability(facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == Capabilities.AUDIO_SOURCE) {
			return Capabilities.AUDIO_SOURCE.cast(this);
		} else if (capability == Capabilities.AUDIO_RECEIVER) {
			return Capabilities.AUDIO_RECEIVER.cast(this);
		} else {
			return null;
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
			if (packet.getData() instanceof IAudioDataPCM && !receivedPacket.contains(packet)) {
				receivedPacket.add(packet);
			}
			return true;
		} else {
			return false;
		}
	}
}
