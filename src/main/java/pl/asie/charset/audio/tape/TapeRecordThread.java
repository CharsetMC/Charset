package pl.asie.charset.audio.tape;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import net.minecraftforge.fml.common.Loader;

import javax.sound.sampled.AudioFormat;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.audio.repack.com.laszlosystems.libresample4j.Resampler;
import pl.asie.charset.lib.utils.DFPWM;

public class TapeRecordThread implements Runnable {
	public static String[] getSupportedExtensions() {
		List<String> exts = new ArrayList<String>();
		exts.add("ogg");
		exts.add("wav");
		if (Loader.isModLoaded("NotEnoughCodecs")) {
			exts.add("mp3");
			exts.add("mp4");
		}
		return exts.toArray(new String[exts.size()]);
	}

	private static final Resampler RESAMPLER = new Resampler(true, 0.01, 100);
	private static final DFPWM CODEC = new DFPWM();
	private final File file;
	private final PartTapeDrive owner;
	private int sampleRate = 48000;
	private String statusBar = "Encoding...";

	private float clamp(float in, float min, float max) {
		return in < min ? min : (in > max ? max : in);
	}

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
			} else if ("mp4".equals(ext)) {
				codec = (ICodec) getClass().getClassLoader().loadClass("openmods.codecs.adapters.CodecMP4").newInstance();
			}

			if (codec == null) {
				statusBar = "Unsupported format!";
				Thread.sleep(1250);
				return;
			}

			codec.initialize(file.toURI().toURL());
			buffer = codec.readAll();

			if (buffer == null) {
				statusBar = "Failed to load!";
				Thread.sleep(1250);
				return;
			}

			statusBar = "Reticulating splines...";

			float[] output = new float[buffer.audioData.length / buffer.audioFormat.getChannels() / (buffer.audioFormat.getSampleSizeInBits() / 8)];
			int si = 0;

			float min = 0.0f;
			float max = 0.0f;

			for(int i = 0; i < output.length; i++) {
				int v = 0;

				for(int j = 0; j < buffer.audioFormat.getChannels(); j++) {
					int s = 0;
					if (buffer.audioFormat.getSampleSizeInBits() == 16) {
						int l = 0xFF&(int)buffer.audioData[si++];
						int h = 0xFF&(int)buffer.audioData[si++];
						s = buffer.audioFormat.isBigEndian() ? ((l << 8) | h) : (l | (h<<8));
						s &= 0xFFFF;
					} else {
						s = buffer.audioData[si++];
						s &= 0xFF;
						s <<= 8;
					}

					if (buffer.audioFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
						s = (s >= 0x8000 ? s - 0x10000 : s);
					} else {
						s -= 0x8000;
					}

					v += s;
				}

				v = (v*2+buffer.audioFormat.getChannels())/(buffer.audioFormat.getChannels()*2);
				output[i] = clamp((float) v / 0x8000, -1.0f, 1.0f);
				if (output[i] < min) {
					min = output[i];
				}
				if (output[i] > max) {
					max = output[i];
				}
			}

			float multiplier = min != 0.0f || max != 0.0f ? 1.0f / Math.max(0 - min, max) : 1.0f;

			double factor = sampleRate / buffer.audioFormat.getSampleRate();
			FloatBuffer resampledBuffer = FloatBuffer.allocate((int) Math.ceil(output.length * factor) + 1024);
			RESAMPLER.process(factor, FloatBuffer.wrap(output), true, resampledBuffer);
			float[] resampledOutput = resampledBuffer.array();
			byte[] preEncodeOutput = new byte[resampledBuffer.position()];

			for (int i = 0; i < preEncodeOutput.length; i++) {
				preEncodeOutput[i] = (byte) (clamp(resampledOutput[i] * multiplier, -1.0f, 1.0f) * 127);
			} 

			statusBar = "Encoding...";

			byte[] finalOutput = new byte[(preEncodeOutput.length + 7) >> 3];
			CODEC.compress(finalOutput, preEncodeOutput, 0, 0, finalOutput.length);

			for (int i = 0; i < finalOutput.length; i += 1024) {
				int len = Math.min(finalOutput.length - i, 1024);
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
