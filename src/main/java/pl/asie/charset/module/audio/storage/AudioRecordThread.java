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

import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FilenameUtils;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.codecs.CodecIBXM;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import pl.asie.charset.lib.audio.codec.DFPWM;
import pl.asie.charset.module.audio.util.AudioResampler;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioRecordThread implements Runnable {
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
				codec = (ICodec) AudioRecordThread.class.getClassLoader().loadClass("openmods.codecs.adapters.CodecMP3").newInstance();
			} else if ("mp4".equals(ext) || "m4a".equals(ext)) {
				codec = (ICodec) AudioRecordThread.class.getClassLoader().loadClass("openmods.codecs.adapters.CodecMP4").newInstance();
			} else if ("aac".equals(ext)) {
				codec = (ICodec) AudioRecordThread.class.getClassLoader().loadClass("openmods.codecs.adapters.CodecADTS").newInstance();
			} else if ("flac".equals(ext)) {
				codec = (ICodec) AudioRecordThread.class.getClassLoader().loadClass("openmods.codecs.adapters.CodecFLAC").newInstance();
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
	private int sampleRate = ItemQuartzDisc.DEFAULT_SAMPLE_RATE;
	private String statusBar = "Encoding...";

	public AudioRecordThread(File f, int maxSize) {
		this.file = f;
		this.maxSize = maxSize;
	}

	public String getStatusBar() {
		return statusBar != null ? statusBar : "???";
	}

	private void showError(String s) {
		statusBar = s;
		try {
			Thread.sleep(1250);
		} catch (InterruptedException e) {

		}
	}

	@Override
	public void run() {
		try {
			String ext = FilenameUtils.getExtension(file.getName());
			SoundBuffer buffer;

			statusBar = "Loading...";

			ICodec codec = getCodec(ext);

			if (codec == null) {
				showError("Unsupported format!");
				return;
			}

			codec.initialize(file.toURI().toURL());
			if (!codec.initialized()) {
				showError("Failed to load!");
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
						showError("Failed to load!");
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

			byte[] preEncodeOutput = AudioResampler.toSigned8(data, format.getSampleSizeInBits(),
					format.getChannels(), format.isBigEndian(),
					format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED,
					(int) format.getSampleRate(), sampleRate, true);

			statusBar = "Encoding...";

			byte[] finalOutput = new byte[(preEncodeOutput.length + 7) >> 3];
			CODEC.compress(finalOutput, preEncodeOutput, 0, 0, finalOutput.length);

			for (int i = 0; i < finalOutput.length; i += PACKET_SIZE) {
				int len = Math.min(finalOutput.length - i, PACKET_SIZE);
				byte[] dataEnc = new byte[len];
				System.arraycopy(finalOutput, i, dataEnc, 0, len);

				statusBar = "Uploading (" + (i * 100 / finalOutput.length) + "%)...";

				CharsetAudioStorage.packet.sendToServer(new PacketDriveData(dataEnc, finalOutput.length, (i + len) >= finalOutput.length));
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
