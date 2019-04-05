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

package pl.asie.charset.lib.audio;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
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
	public void writeData(PacketBuffer buf) {
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
	public void readData(INetHandler handler, PacketBuffer buf) {
		id = buf.readInt();
		packet = new AudioPacket();
		packet.readData(buf);
	}

	@Override
	public void apply(INetHandler handler) {
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
				stream.play((float) sink.getPos().x, (float) sink.getPos().y, (float) sink.getPos().z,
						sink.getDistance(), sink.getVolume() * packet.getVolume());
			} catch (Exception e) {

			}
		}
	}
}
