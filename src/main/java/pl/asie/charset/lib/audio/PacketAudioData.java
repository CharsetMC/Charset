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

import net.minecraft.network.INetHandler;
import pl.asie.charset.api.audio.AudioPacket;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.api.audio.IPCMPacket;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.audio.manager.AudioStreamManagerClient;
import pl.asie.charset.lib.audio.manager.AudioStreamOpenAL;
import pl.asie.charset.lib.audio.manager.IAudioStream;
import pl.asie.charset.lib.network.Packet;

public class PacketAudioData extends Packet {
	private AudioPacket packet;

	public PacketAudioData() {
		super();
	}

	protected PacketAudioData(AudioPacketCharset packet) {
		super();
		this.packet = packet;
	}

	@Override
	public void writeData(ByteBuf buf) {
		packet.writeData(buf);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		AudioPacket packet = AudioPacket.create(buf);
		if (!(packet instanceof AudioPacketCharset)) {
			return;
		}

		IPCMPacket pcmPacket = (IPCMPacket) packet;
		if (pcmPacket.getPCMSampleSizeBits() != 8) {
			ModCharsetLib.logger.error("PacketAudioData cannot read non-8-bit packets!");
			return;
		}

		byte[] data = pcmPacket.getPCMData();
		if (pcmPacket.getPCMSigned()) {
			byte[] data2 = new byte[data.length];
			for (int i = 0; i < data.length; i++) {
				data2[i] = (byte) (data[i] ^ 0x80);
			}
			data = data2;
		}

		int id = ((AudioPacketCharset) packet).getId();
		IAudioStream stream = AudioStreamManagerClient.INSTANCE.get(id);
		if (stream == null) {
			stream = new AudioStreamOpenAL(false, false, 8);
			AudioStreamManagerClient.INSTANCE.put(id, stream);
		}

		stream.setSampleRate(pcmPacket.getPCMSampleRate());
		stream.push(data);

		for (AudioSink sink : packet.getSinks()) {
			// TODO
			stream.play((float) sink.getPos().xCoord, (float) sink.getPos().yCoord, (float) sink.getPos().zCoord,
					32.0F, 1.0F);
		}
	}
}
