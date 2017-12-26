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
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.laszlosystems.libresample4j.Resampler;
import org.apache.commons.io.FilenameUtils;

import net.minecraftforge.fml.common.Loader;

import javax.sound.sampled.AudioFormat;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.codecs.CodecIBXM;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.audio.codec.DFPWM;

public class TapeRecordThread implements Runnable {
	public static String[] getSupportedExtensions() {
		List<String> exts = getSupportedExtensionList();
		return exts.toArray(new String[exts.size()]);
	}

	private static void addIfCodec(List<String> exts, String ext) {
		if (getCodec(ext) != null) {
			exts.add(ext);
		}
	}

	public static List<String> getSupportedExtensionList() {
		List<String> exts = new ArrayList<String>();
		for (String s: new String[] { "ogg", "wav", "mod", "s3m", "xm" }) {
			addIfCodec(exts, s);
		}

		if (Loader.isModLoaded("notenoughcodecs")) {
			for (String s: new String[] { "aac", "mp3", "m4a", "mp4", "flac" }) {
				addIfCodec(exts, s);
			}
		}

		return exts;
	}

	private static ICodec getCodec(String ext) {
		try {
			ICodec codec = null;

			if ("ogg".equals(ext)) {
				codec = new CodecJOrbis();
			} else if ("wav".equals(ext)) {
				codec = new CodecWav();
			} else if ("mod".equals(ext) || "s3m".equals(ext) || "xm".equals(ext)) {
				codec = new CodecIBXM();
			} else if ("mp3".equals(ext)) {
				codec = (ICodec) TapeRecordThread.class.getClassLoader().loadClass("openmods.codecs.adapters.CodecMP3").newInstance();
			} else if ("mp4".equals(ext) || "m4a".equals(ext)) {
				codec = (ICodec) TapeRecordThread.class.getClassLoader().loadClass("openmods.codecs.adapters.CodecMP4").newInstance();
			} else if ("aac".equals(ext)) {
				codec = (ICodec) TapeRecordThread.class.getClassLoader().loadClass("openmods.codecs.adapters.CodecADTS").newInstance();
			} else if ("flac".equals(ext)) {
				codec = (ICodec) TapeRecordThread.class.getClassLoader().loadClass("openmods.codecs.adapters.CodecFLAC").newInstance();
			}

			return codec;
		} catch (Exception e) {
			return null;
		}
	}

	private static final int PACKET_SIZE = 8192;
	private static final DFPWM CODEC = new DFPWM();
	private final File file;
	private final int maxSize;
	private final PartTapeDrive owner;
	private int sampleRate = ItemTape.DEFAULT_SAMPLE_RATE;
	private String statusBar = "Encoding...";

	public TapeRecordThread(File f, int maxSize, PartTapeDrive owner) {
		this.file = f;
		this.maxSize = maxSize;
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

			statusBar = "Loading...";

			ICodec codec = getCodec(ext);

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


			byte[] data = null;
			AudioFormat format;

			if (codec instanceof CodecIBXM) {
				format = codec.getAudioFormat();
				long maxLength = (long) maxSize * format.getSampleSizeInBits() / 8 * format.getChannels() * (int) format.getSampleRate() / sampleRate;
				while (!codec.endOfStream() && (data == null || data.length < maxLength)) {
					buffer = codec.read();
					if (buffer == null) {
						statusBar = "Failed to load!";
						Thread.sleep(1250);
						return;
					}

					if (data == null) {
						data = buffer.audioData;
					} else {
						byte[] oldData = data;
						data = new byte[oldData.length + buffer.audioData.length];
						System.arraycopy(oldData, 0, data, 0, oldData.length);
						System.arraycopy(buffer.audioData, 0, data, oldData.length, buffer.audioData.length);
					}
				}
			} else {
				buffer = codec.readAll();
				data = buffer.audioData;
				format = buffer.audioFormat;
			}

			statusBar = "Reticulating splines...";

			byte[] preEncodeOutput = TapeResampler.toSigned8(data, format.getSampleSizeInBits(),
					format.getChannels(), format.isBigEndian(),
					format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED,
					(int) format.getSampleRate(), sampleRate, true);

			statusBar = "Encoding...";

			byte[] finalOutput = new byte[(preEncodeOutput.length + 7) >> 3];
			CODEC.compress(finalOutput, preEncodeOutput, 0, 0, finalOutput.length);

			for (int i = 0; i < finalOutput.length; i += PACKET_SIZE) {
				int len = Math.min(finalOutput.length - i, PACKET_SIZE);
				byte[] outData = new byte[len];
				System.arraycopy(finalOutput, i, outData, 0, len);

				statusBar = "Uploading (" + (i * 100 / finalOutput.length) + "%)...";

				ModCharsetAudio.packet.sendToServer(new PacketDriveRecord(owner, outData, finalOutput.length, (i + len) >= finalOutput.length));
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
