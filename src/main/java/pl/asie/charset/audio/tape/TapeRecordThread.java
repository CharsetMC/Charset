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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import org.apache.commons.io.FilenameUtils;

import net.minecraftforge.fml.common.Loader;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.audio.codec.DFPWM;

public class TapeRecordThread implements Runnable {
	public static String[] getSupportedExtensions() {
		List<String> exts = new ArrayList<String>();
		exts.add("ogg");
		exts.add("wav");
		if (Loader.isModLoaded("NotEnoughCodecs")) {
			exts.add("aac");
			exts.add("mp3");
			exts.add("m4a");
			exts.add("mp4");
			exts.add("flac");
		}
		return exts.toArray(new String[exts.size()]);
	}

	private static final int PACKET_SIZE = 8192;
	private static final DFPWM CODEC = new DFPWM();
	private final File file;
	private final PartTapeDrive owner;
	private int sampleRate = ItemTape.DEFAULT_SAMPLE_RATE;
	private String statusBar = "Encoding...";

	public TapeRecordThread(File f, PartTapeDrive owner) {
		this.file = f;
		this.owner = owner;
	}

	public String getStatusBar() {
		return statusBar != null ? statusBar : "???";
	}

	@Override
	public void run() {
		try {
			String ext = FilenameUtils.getExtension(file.getName());
			SoundBuffer buffer;
			ICodec codec = null;

			statusBar = "Loading...";

			if ("ogg".equals(ext)) {
				codec = new CodecJOrbis();
			} else if ("wav".equals(ext)) {
				codec = new CodecWav();
			} else if ("mp3".equals(ext)) {
				codec = (ICodec) getClass().getClassLoader().loadClass("openmods.codecs.adapters.CodecMP3").newInstance();
			} else if ("mp4".equals(ext) || "m4a".equals(ext)) {
				codec = (ICodec) getClass().getClassLoader().loadClass("openmods.codecs.adapters.CodecMP4").newInstance();
			} else if ("aac".equals(ext)) {
				codec = (ICodec) getClass().getClassLoader().loadClass("openmods.codecs.adapters.CodecADTS").newInstance();
			} else if ("flac".equals(ext)) {
				codec = (ICodec) getClass().getClassLoader().loadClass("openmods.codecs.adapters.CodecFLAC").newInstance();
			}

			if (codec == null) {
				statusBar = "Unsupported format!";
				Thread.sleep(1250);
				return;
			}

			codec.initialize(file.toURI().toURL());
			if (!codec.initialized()) {
				statusBar = "Failed to load!";
				Thread.sleep(1250);
				return;
			}
			buffer = codec.readAll();

			if (buffer == null) {
				statusBar = "Failed to load!";
				Thread.sleep(1250);
				return;
			}

			statusBar = "Reticulating splines...";

			byte[] preEncodeOutput = TapeResampler.toSigned8(buffer.audioData, buffer.audioFormat.getSampleSizeInBits(),
					buffer.audioFormat.getChannels(), buffer.audioFormat.isBigEndian(),
					buffer.audioFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED,
					(int) buffer.audioFormat.getSampleRate(), sampleRate, true);

			statusBar = "Encoding...";

			byte[] finalOutput = new byte[(preEncodeOutput.length + 7) >> 3];
			CODEC.compress(finalOutput, preEncodeOutput, 0, 0, finalOutput.length);

			for (int i = 0; i < finalOutput.length; i += PACKET_SIZE) {
				int len = Math.min(finalOutput.length - i, PACKET_SIZE);
				byte[] data = new byte[len];
				System.arraycopy(finalOutput, i, data, 0, len);

				statusBar = "Uploading (" + (i * 100 / finalOutput.length) + "%)...";

				ModCharsetAudio.packet.sendToServer(new PacketDriveRecord(owner, data, finalOutput.length, (i + len) >= finalOutput.length));
			}

			statusBar = "Uploading (100%)...";
			Thread.sleep(250);

			statusBar = "Uploaded!";
			Thread.sleep(1250);
		} catch (Exception e) {
			e.printStackTrace();
			statusBar = "Strange error!";
			try {
				Thread.sleep(1250);
			} catch (InterruptedException ee) {

			}
		}
	}
}
