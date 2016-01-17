package pl.asie.charset.audio.manager;

import java.util.HashMap;

import pl.asie.charset.api.audio.IAudioSource;

public class AudioStreamManager {
	private final HashMap<IAudioSource, IAudioStream> streams = new HashMap<IAudioSource, IAudioStream>();

	public void put(IAudioSource source, IAudioStream stream) {
		streams.put(source, stream);
	}

	public IAudioStream get(IAudioSource id) {
		return streams.get(id);
	}

	public void remove(IAudioSource id) {
		if (streams.containsKey(id)) {
			streams.get(id).stop();
			streams.remove(id);
		}
	}

	public void removeAll() {
		for (IAudioSource source : streams.keySet()) {
			streams.get(source).stop();
		}
		streams.clear();
	}
}
