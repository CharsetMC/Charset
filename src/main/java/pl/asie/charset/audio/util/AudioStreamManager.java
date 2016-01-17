package pl.asie.charset.audio.util;

public class AudioStreamManager {
	private int currentId = 0;

	public int add(IAudioStream stream) {
		return currentId++;
	}

	public void remove(int id) {

	}

	public void set(int id, IAudioStream stream) {

	}

	public IAudioStream get(int id) {
		return null;
	}
}
