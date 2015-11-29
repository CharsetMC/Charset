package pl.asie.charset.audio.client;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import pl.asie.charset.audio.util.AudioStreamManager;
import pl.asie.charset.audio.util.IAudioStream;

public class AudioStreamManagerClient extends AudioStreamManager {
	private final TIntObjectMap<IAudioStream> streams = new TIntObjectHashMap<IAudioStream>();

	@Override
	public int add(IAudioStream stream) {
		int id = super.add(stream);
		streams.put(id, stream);
		return id;
	}

	@Override
	public void remove(int id) {
		if (streams.containsKey(id)) {
			streams.get(id).stop();
			streams.remove(id);
		}
		streams.remove(id);
	}

	@Override
	public void set(int id, IAudioStream stream) {
		streams.put(id, stream);
	}

	@Override
	public IAudioStream get(int id) {
		return streams.get(id);
	}
}
