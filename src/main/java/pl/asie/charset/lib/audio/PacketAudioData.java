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

package pl.asie.charset.lib.audio;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.network.INetHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.api.audio.IAudioDataPCM;
import pl.asie.charset.lib.audio.manager.AudioStreamManagerClient;
import pl.asie.charset.lib.audio.manager.AudioStreamOpenAL;
import pl.asie.charset.lib.audio.manager.IAudioStream;
import pl.asie.charset.lib.audio.types.IDataGameSound;
import pl.asie.charset.lib.network.Packet;

public class PacketAudioData extends Packet {
	private int id;
	private AudioPacket packet;

	public PacketAudioData() {
		super();
	}

	public PacketAudioData(int id, AudioPacket packet) {
		super();
		this.id = id;
		this.packet = packet;
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(id);
		packet.writeData(buf);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	private void playSoundNote(AudioPacket packet, IDataGameSound sound) {
		for (AudioSink sink : packet.getSinks()) {
			Minecraft.getMinecraft().getSoundHandler().playSound(
					new PositionedSoundRecord(new SoundEvent(new ResourceLocation(sound.getSoundName())),
							SoundCategory.BLOCKS, 3.0F * sink.getVolume() * packet.getVolume(),
							sound.getSoundPitch(), new BlockPos(sink.getPos()))
			);
		}
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		int id = buf.readInt();
		AudioPacket packet = new AudioPacket();
		packet.readData(buf);
	}

	@Override
	public void apply() {
		AudioData audioData = packet.getData();
		if (audioData instanceof IDataGameSound) {
			IDataGameSound sound = (IDataGameSound) audioData;
			playSoundNote(packet, sound);
			return;
		}

		if (!(audioData instanceof IAudioDataPCM) || ((IAudioDataPCM) audioData).getSampleSize() != 1) {
			// Nope!
			return;
		}

		IAudioDataPCM pcmPacket = (IAudioDataPCM) audioData;
		byte[] data = pcmPacket.getSamplePCMData();
		if (pcmPacket.isSampleSigned()) {
			byte[] data2 = new byte[data.length];
			for (int i = 0; i < data.length; i++) {
				data2[i] = (byte) (data[i] ^ 0x80);
			}
			data = data2;
		}

		IAudioStream stream = AudioStreamManagerClient.INSTANCE.get(id);
		if (stream == null) {
			stream = new AudioStreamOpenAL(false, false, 8);
			AudioStreamManagerClient.INSTANCE.put(id, stream);
		}

		stream.setSampleRate(pcmPacket.getSampleRate());
		stream.push(data);

		for (AudioSink sink : packet.getSinks()) {
			try {
				stream.play((float) sink.getPos().xCoord, (float) sink.getPos().yCoord, (float) sink.getPos().zCoord,
						sink.getDistance(), sink.getVolume() * packet.getVolume());
			} catch (Exception e) {

			}
		}
	}
}
