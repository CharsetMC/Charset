/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.lib.audio.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import pl.asie.charset.ModCharset;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AudioStreamOpenAL implements IAudioStream {
	public class SourceEntry {
		public final float x, y, z;
		public final IntBuffer src;
		public int receivedPackets;

		public SourceEntry(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
			src = BufferUtils.createIntBuffer(1);
			AL10.alGenSources(src);
		}
	}

	private final Set<SourceEntry> sources = new HashSet<SourceEntry>();
	private final ArrayList<IntBuffer> buffersPlayed = new ArrayList<IntBuffer>();
	private final int BUFFER_PACKETS, AUDIO_FORMAT;

	private IntBuffer currentBuffer;

	private int sampleRate = 32768;

	public AudioStreamOpenAL(boolean sixteenBit, boolean stereo, int bufferPackets) {
		super();
		BUFFER_PACKETS = bufferPackets;
		if (sixteenBit) {
			AUDIO_FORMAT = stereo ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
		} else {
			AUDIO_FORMAT = stereo ? AL10.AL_FORMAT_STEREO8 : AL10.AL_FORMAT_MONO8;
		}

		reset();
	}

	@Override
	public void setSampleRate(int rate) {
		sampleRate = rate;
	}

	@Override
	public void reset() {
		buffersPlayed.clear();
		stop();
	}

	@SideOnly(Side.CLIENT)
	private double getDistance(float x, float y, float z) {
		Vec3d pos = Minecraft.getMinecraft().player.getPositionVector();
		return pos.distanceTo(new Vec3d(x, y, z));
	}

	@Override
	public void push(byte[] data) {
		// Prepare buffers
		if (currentBuffer == null) {
			currentBuffer = BufferUtils.createIntBuffer(1);
		} else {
			for (SourceEntry source : sources) {
				int processed = AL10.alGetSourcei(source.src.get(0), AL10.AL_BUFFERS_PROCESSED);
				if (processed > 0) {
					AL10.alSourceUnqueueBuffers(source.src.get(0), currentBuffer);
				}
			}
		}

		AL10.alGenBuffers(currentBuffer);
		AL10.alBufferData(currentBuffer.get(0), AUDIO_FORMAT, (ByteBuffer) (BufferUtils.createByteBuffer(data.length).put(data).flip()), sampleRate);

		synchronized (buffersPlayed) {
			buffersPlayed.add(currentBuffer);
		}
	}

	@Override
	public void play(float x, float y, float z, float distance, float volume) {
		FloatBuffer sourcePos = (FloatBuffer) (BufferUtils.createFloatBuffer(3).put(new float[]{x, y, z}).rewind());
		FloatBuffer sourceVel = (FloatBuffer) (BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind());

		SourceEntry source = null;
		for (SourceEntry entry : sources) {
			if (entry.x == x && entry.y == y && entry.z == z) {
				source = entry;
				continue;
			}
		}
		if (source == null) {
			source = new SourceEntry(x, y, z);
			sources.add(source);
		}

		// Calculate distance
		float playerDistance = (float) getDistance(x, y, z);
		float distanceUsed = distance * (0.2F + (volume * 0.8F));
		float distanceReal = 1 - (playerDistance / distanceUsed);

		float gain = distanceReal * volume * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
		if (gain < 0.0F) {
			gain = 0.0F;
		} else if (gain > 1.0F) {
			gain = 1.0F;
		}

		// Set settings
		AL10.alSourcei(source.src.get(0), AL10.AL_LOOPING, AL10.AL_FALSE);
		AL10.alSourcef(source.src.get(0), AL10.AL_PITCH, 1.0f);
		AL10.alSourcef(source.src.get(0), AL10.AL_GAIN, gain);
		AL10.alSource(source.src.get(0), AL10.AL_POSITION, sourcePos);
		AL10.alSource(source.src.get(0), AL10.AL_VELOCITY, sourceVel);
		AL10.alSourcef(source.src.get(0), AL10.AL_ROLLOFF_FACTOR, 0.0f);

		// Play audio
		AL10.alSourceQueueBuffers(source.src.get(0), currentBuffer);

		int state = AL10.alGetSourcei(source.src.get(0), AL10.AL_SOURCE_STATE);

		if (source.receivedPackets > BUFFER_PACKETS && state != AL10.AL_PLAYING) AL10.alSourcePlay(source.src.get(0));
		else if (source.receivedPackets <= BUFFER_PACKETS) AL10.alSourcePause(source.src.get(0));

		source.receivedPackets++;
	}

	@Override
	public void stop() {
		int sourceCount = sources.size();
		for (SourceEntry source : sources) {
			AL10.alSourceStop(source.src.get(0));
			AL10.alDeleteSources(source.src.get(0));
		}
		sources.clear();

		int bufferCount = 0;
		if (buffersPlayed != null) {
			synchronized (buffersPlayed) {
				if (currentBuffer != null) {
					buffersPlayed.add(currentBuffer);
				}

				for (IntBuffer b : buffersPlayed) {
					b.rewind();
					for (int i = 0; i < b.limit(); i++) {
						int buffer = b.get(i);
						if (AL10.alIsBuffer(buffer)) {
							AL10.alDeleteBuffers(buffer);
							bufferCount++;
						}
					}
				}
				buffersPlayed.clear();
			}
		}

		ModCharset.logger.debug("Cleaned " + bufferCount + " buffers and " + sourceCount + " sources.");
	}
}
