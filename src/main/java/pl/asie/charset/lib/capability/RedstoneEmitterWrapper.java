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

import java.util.Collection;

import net.minecraftforge.common.capabilities.Capability;

import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.lib.Capabilities;

public class RedstoneEmitterWrapper implements ICapabilityWrapper<IRedstoneEmitter> {
	@Override
	public IRedstoneEmitter wrapImplementations(Collection<IRedstoneEmitter> collection) {
		int data = 0;

		for (IRedstoneEmitter emitter : collection) {
			data = Math.max(data, emitter.getRedstoneSignal());
		}

		return new DefaultRedstoneEmitter(data);
	}
}
