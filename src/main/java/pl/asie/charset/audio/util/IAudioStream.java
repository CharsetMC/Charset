package pl.asie.charset.audio.util;

public interface IAudioStream {
	void setHearing(float distance, float volume);
	void setSampleRate(int sampleRate);

	void reset();
	void stop();

	void push(byte[] data);
	void play(int x, int y, int z);
}
