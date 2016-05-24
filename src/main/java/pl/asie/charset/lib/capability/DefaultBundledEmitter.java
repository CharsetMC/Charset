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

package pl.asie.charset.lib.capability;

import pl.asie.charset.api.wires.IBundledEmitter;

public class DefaultBundledEmitter implements IBundledEmitter {
	private byte[] data;

	public DefaultBundledEmitter(byte[] data) {
		this.data = data;
	}

	public DefaultBundledEmitter() {
		this.data = new byte[16];
	}

	@Override
	public byte[] getBundledSignal() {
		return data;
	}

	public void emit(byte[] data) {
		if (data == null || data.length != 16) {
			data = new byte[16];
		} else {
			this.data = data;
		}
	}
}
