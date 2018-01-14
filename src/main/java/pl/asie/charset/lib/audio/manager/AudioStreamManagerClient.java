/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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
