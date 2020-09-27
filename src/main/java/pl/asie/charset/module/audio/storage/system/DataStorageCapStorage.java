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

package pl.asie.charset.module.audio.storage.system;

import java.io.IOException;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.common.util.Constants;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.tape.IDataStorage;

public class DataStorageCapStorage implements Capability.IStorage<IDataStorage> {
	@Override
	public NBTBase writeNBT(Capability<IDataStorage> capability, IDataStorage instance, EnumFacing side) {
		if (instance.getUniqueId() != null) {
			NBTTagCompound compoundBase = new NBTTagCompound();
			NBTTagCompound compound = new NBTTagCompound();

			compound.setInteger("size", instance.getSize());
			compound.setInteger("pos", instance.getPosition());
			compound.setString("uid", instance.getUniqueId());

			try {
				instance.onUnload();
			} catch (IOException e) {
				ModCharset.logger.error("Could not save a DataStorage! (ID: " + instance.getUniqueId() + ")");
				e.printStackTrace();
			}

			compoundBase.setTag("charset:data_storage", compound);
			return compoundBase;
		} else {
			return null;
		}
	}

	@Override
	public void readNBT(Capability<IDataStorage> capability, IDataStorage instance, EnumFacing side, NBTBase nbt) {
		if (nbt instanceof NBTTagCompound) {
			NBTTagCompound compoundBase = (NBTTagCompound) nbt;
			if (compoundBase.hasKey("charset:data_storage", Constants.NBT.TAG_COMPOUND)) {
				NBTTagCompound compound = compoundBase.getCompoundTag("charset:data_storage");
				instance.initialize(compound.getString("uid"), compound.getInteger("pos"), compound.getInteger("size"));
			}
		}
	}
}
