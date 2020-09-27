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

package pl.asie.charset.lib.capability.redstone;

import pl.asie.charset.api.wires.IBundledEmitter;

import java.util.List;
import java.util.function.Function;

public class BundledEmitterCombiner implements Function<List<IBundledEmitter>, IBundledEmitter> {
	@Override
	public IBundledEmitter apply(List<IBundledEmitter> collection) {
		byte[] data = new byte[16];

		for (IBundledEmitter emitter : collection) {
			byte[] dataIn = emitter.getBundledSignal();
			if (dataIn != null) {
				for (int i = 0; i < 16; i++) {
					data[i] = (byte) Math.max(0xFF & (int) dataIn[i], 0xFF & (int) data[i]);
				}
			}
		}

		return new DefaultBundledEmitter(data);
	}
}
