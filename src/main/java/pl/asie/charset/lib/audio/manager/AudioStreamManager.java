package pl.asie.charset.lib.audio.manager;

public abstract class AudioStreamManager {
	public static AudioStreamManager INSTANCE;

	public abstract void put(int id, IAudioStream stream);
	public abstract IAudioStream get(int id);
	public abstract int create();
	public abstract void remove(int id);
	public abstract void removeAll();
}
