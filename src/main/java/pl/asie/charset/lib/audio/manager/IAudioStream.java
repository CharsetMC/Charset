package pl.asie.charset.lib.audio.manager;

public interface IAudioStream {
	void setSampleRate(int sampleRate);

	void reset();

	void stop();

	void push(byte[] data);

	void play(float x, float y, float z, float distance, float volume);
}
