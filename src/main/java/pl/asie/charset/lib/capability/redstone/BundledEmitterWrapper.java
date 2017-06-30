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

package pl.asie.charset.lib.capability.redstone;

import pl.asie.charset.api.wires.IBundledEmitter;

import java.util.List;
import java.util.function.Function;

public class BundledEmitterWrapper implements Function<List<IBundledEmitter>, IBundledEmitter> {
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
