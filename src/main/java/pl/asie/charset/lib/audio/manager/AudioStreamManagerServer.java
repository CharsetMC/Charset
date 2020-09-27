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

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Random;

public class AudioStreamManagerServer extends AudioStreamManager {
    private final TIntSet sources = new TIntHashSet();
    private final Random random = new Random();

    @Override
    public void put(int source, IAudioStream stream) {
        sources.add(source);
    }

    @Override
    public IAudioStream get(int id) {
        return null;
    }

    @Override
    public int create() {
        int i = random.nextInt();
        while (sources.contains(i)) {
            i++;
        }
        put(i, null);
        return i;
    }

    @Override
    public void remove(int id) {
        sources.remove(id);
    }

    @Override
    public void removeAll() {
        sources.clear();
    }
}
