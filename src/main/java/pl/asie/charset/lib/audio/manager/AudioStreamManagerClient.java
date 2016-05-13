package pl.asie.charset.lib.audio.manager;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class AudioStreamManagerClient extends AudioStreamManager {
	public AudioStreamManagerClient() {

	}

	private final TIntObjectMap<IAudioStream> streams = new TIntObjectHashMap<IAudioStream>();

	@Override
	public void put(int source, IAudioStream stream) {
		streams.put(source, stream);
	}

	@Override
	public IAudioStream get(int id) {
		return streams.get(id);
	}

	@Override
	public void remove(int id) {
		if (streams.containsKey(id)) {
			streams.get(id).stop();
			streams.remove(id);
		}
	}

	@Override
	public int create() {
		return -1; // Nope
	}

	@Override
	public void removeAll() {
		TIntIterator iterator = streams.keySet().iterator();
		while (iterator.hasNext()) {
			streams.get(iterator.next()).stop();
		}
		streams.clear();
	}
}
