/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
