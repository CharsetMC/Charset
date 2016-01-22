package pl.asie.charset.audio.tape;

import java.io.File;
import java.nio.FloatBuffer;

import org.apache.commons.io.FilenameUtils;

import javax.sound.sampled.AudioFormat;
import paulscode.sound.SoundBuffer;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.audio.repack.com.laszlosystems.libresample4j.Resampler;
import pl.asie.charset.lib.utils.DFPWM;

/**
 * Created by asie on 1/22/16.
 */
public class TapeRecordThread implements Runnable {
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
			SoundBuffer buffer = null;

			statusBar = "Loading...";

			if ("ogg".equals(ext)) {
				CodecJOrbis orbis = new CodecJOrbis();
				orbis.initialize(file.toURI().toURL());
				buffer = orbis.readAll();
			} else if ("wav".equals(ext)) {
				CodecWav wav = new CodecWav();
				wav.initialize(file.toURI().toURL());
				buffer = wav.readAll();
			}

			if (buffer == null) {
				statusBar = "Failed to load!";
				return;
			}

			statusBar = "Reticulating splines...";

			float[] output = new float[buffer.audioData.length / buffer.audioFormat.getChannels() / (buffer.audioFormat.getSampleSizeInBits() / 8)];
			int si = 0;

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
			}

			double factor = sampleRate / buffer.audioFormat.getSampleRate();
			FloatBuffer resampledBuffer = FloatBuffer.allocate((int) Math.ceil(output.length * factor) + 1024);
			RESAMPLER.process(factor, FloatBuffer.wrap(output), true, resampledBuffer);
			float[] resampledOutput = resampledBuffer.array();
			byte[] preEncodeOutput = new byte[resampledBuffer.position()];

			for (int i = 0; i < preEncodeOutput.length; i++) {
				preEncodeOutput[i] = (byte) (clamp(resampledOutput[i], -1.0f, 1.0f) * 127);
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
		}
	}
}
