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

package pl.asie.charset.module.power.steam;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class SteamChunkContainerStorage implements Capability.IStorage<SteamChunkContainer> {
	@Nullable
	@Override
	public NBTBase writeNBT(Capability<SteamChunkContainer> capability, SteamChunkContainer instance, EnumFacing side) {
		NBTTagCompound cpd = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (SteamParticle particle : instance.getParticles()) {
			list.appendTag(particle.serializeNBT());
		}
		cpd.setTag("particles", list);
		return cpd;
	}

	@Override
	public void readNBT(Capability<SteamChunkContainer> capability, SteamChunkContainer instance, EnumFacing side, NBTBase nbt) {
		if (instance.getChunk() != null && instance.getChunk().getWorld() != null && nbt instanceof NBTTagCompound) {
			NBTTagCompound cpd = (NBTTagCompound) nbt;
			if (cpd.hasKey("particles", Constants.NBT.TAG_LIST)) {
				NBTTagList list = cpd.getTagList("particles", Constants.NBT.TAG_COMPOUND);
				instance.getParticles().clear();
				for (int i = 0; i < list.tagCount(); i++) {
					SteamParticle p = new SteamParticle(instance.getChunk().getWorld());
					p.deserializeNBT(list.getCompoundTagAt(i));
					instance.getParticles().add(p);
				}
			}
		}
	}
}
