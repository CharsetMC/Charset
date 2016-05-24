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

import pl.asie.charset.api.wires.IRedstoneEmitter;

public class DefaultRedstoneEmitter implements IRedstoneEmitter {
	private int data;

	public DefaultRedstoneEmitter(int data) {
		emit(data);
	}

	public DefaultRedstoneEmitter() {
		emit(0);
	}

	@Override
	public int getRedstoneSignal() {
		return data;
	}

	public void emit(int data) {
		if (data > 15) {
			this.data = 15;
		} else if (data < 0) {
			this.data = 0;
		} else {
			this.data = data;
		}
	}
}
