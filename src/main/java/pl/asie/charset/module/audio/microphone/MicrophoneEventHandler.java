/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.audio.microphone;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;
import pl.asie.charset.lib.audio.codec.DFPWM;
import pl.asie.charset.lib.audio.types.AudioDataDFPWM;
import pl.asie.charset.module.audio.storage.AudioResampler;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MicrophoneEventHandler {
	private DFPWM dfpwm;
	private int recordingDeviceFreq;
	private ALCdevice recordingDevice;

	private void createRecordingDevice() {
		if (recordingDevice != null) return;

		recordingDeviceFreq = 44100;
		recordingDevice = ALC11.alcCaptureOpenDevice(null, recordingDeviceFreq, AL10.AL_FORMAT_MONO8, (recordingDeviceFreq / 20) * 4);

		if (recordingDevice != null) {
			ALC11.alcCaptureStart(recordingDevice);
			dfpwm = new DFPWM();
		}
	}

	private void freeRecordingDevice() {
		if (recordingDevice == null) return;

		ALC11.alcCaptureStop(recordingDevice);
		ALC11.alcCaptureCloseDevice(recordingDevice);
		recordingDevice = null;
		dfpwm = null;
	}

	private boolean isValidMicrophone(ItemStack stack) {
		return stack.getItem() instanceof ItemMicrophone
				&& ((ItemMicrophone) stack.getItem()).hasReceiver(stack);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			List<ItemStack> microphones = new ArrayList<>();

			if (player != null) {
				ItemStack stackMain = player.getHeldItemMainhand();
				ItemStack stackOff = player.getHeldItemOffhand();
				if (isValidMicrophone(stackMain)) microphones.add(stackMain);
				if (isValidMicrophone(stackOff)) microphones.add(stackOff);
			}

			if (!microphones.isEmpty()) {
				createRecordingDevice();
				if (recordingDevice != null) {
					int bufSize = recordingDeviceFreq / 20;
					IntBuffer sampleCountBuf = BufferUtils.createIntBuffer(1);
					ByteBuffer buffer = BufferUtils.createByteBuffer(bufSize);

					ALC10.alcGetInteger(recordingDevice, ALC11.ALC_CAPTURE_SAMPLES, sampleCountBuf);
					int sampleCount = sampleCountBuf.get(0);
					System.out.println(sampleCount);

					while (sampleCount >= bufSize) {
						ALC11.alcCaptureSamples(recordingDevice, buffer, bufSize);
						buffer.rewind();
						sampleCount -= bufSize;

						byte[] encData = new byte[bufSize];
						buffer.get(encData);
						buffer.rewind();

						encData = AudioResampler.toSigned8(encData, 8, 1, false, false, recordingDeviceFreq, 48000, false);

						byte[] dfpwmData = new byte[encData.length / 8];
						dfpwm.compress(dfpwmData, encData, 0, 0, dfpwmData.length);
						AudioDataDFPWM dataDFPWM = new AudioDataDFPWM(dfpwmData, encData.length / 48);

						for (ItemStack microphone : microphones) {
							int dim = ((ItemMicrophone) microphone.getItem()).getReceiverDimension(microphone);
							if (player.getEntityWorld().provider.getDimension() != dim) continue;

							BlockPos pos = ((ItemMicrophone) microphone.getItem()).getReceiverPos(microphone);
							TileEntity tile = player.getEntityWorld().getTileEntity(pos);

							if (tile != null) {
								CharsetAudioMicrophone.packet.sendToServer(new PacketSendDataTile(
									tile, ((ItemMicrophone) microphone.getItem()).getId(microphone), dataDFPWM
								));
							}
						}
					}
				}
			} else {
				freeRecordingDevice();
			}
		}
	}
}
