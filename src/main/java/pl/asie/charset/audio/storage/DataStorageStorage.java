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

package pl.asie.charset.audio.storage;

import java.io.IOException;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import pl.asie.charset.api.audio.IDataStorage;
import pl.asie.charset.audio.ModCharsetAudio;

public class DataStorageStorage implements Capability.IStorage<IDataStorage> {
	@Override
	public NBTBase writeNBT(Capability<IDataStorage> capability, IDataStorage instance, EnumFacing side) {
		if (instance.getUniqueId() != null) {
			NBTTagCompound compound = new NBTTagCompound();

			compound.setInteger("size", instance.getSize());
			compound.setString("uid", instance.getUniqueId());

			try {
				instance.onUnload();
			} catch (IOException e) {
				ModCharsetAudio.logger.error("Could not save a DataStorage! (ID: " + instance.getUniqueId() + ")");
				e.printStackTrace();
			}

			return compound;
		} else {
			return null;
		}
	}

	@Override
	public void readNBT(Capability<IDataStorage> capability, IDataStorage instance, EnumFacing side, NBTBase nbt) {
		if (nbt instanceof NBTTagCompound) {
			NBTTagCompound compound = (NBTTagCompound) nbt;
			if (compound.hasKey("uid")) {
				instance.initialize(compound.getString("uid"), 0, compound.getInteger("size"));
			}
		}
	}
}
