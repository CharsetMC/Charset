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

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import pl.asie.charset.api.wires.IRedstoneEmitter;

public class DefaultRedstoneEmitterStorage implements Capability.IStorage<IRedstoneEmitter> {
	@Override
	public NBTBase writeNBT(Capability<IRedstoneEmitter> capability, IRedstoneEmitter instance, EnumFacing side) {
		if (instance instanceof DefaultRedstoneEmitter) {
			NBTTagCompound cpd = new NBTTagCompound();
			cpd.setInteger("s", instance.getRedstoneSignal());
			return cpd;
		}
		return null;
	}

	@Override
	public void readNBT(Capability<IRedstoneEmitter> capability, IRedstoneEmitter instance, EnumFacing side, NBTBase nbt) {
		if (instance instanceof DefaultRedstoneEmitter && nbt instanceof NBTTagCompound) {
			NBTTagCompound cpd = (NBTTagCompound) nbt;
			if (cpd.hasKey("s")) {
				((DefaultRedstoneEmitter) instance).emit(cpd.getInteger("s"));
			}
		}
	}
}
