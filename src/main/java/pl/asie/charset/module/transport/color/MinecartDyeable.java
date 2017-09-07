/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.transport.color;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class MinecartDyeable {
	private EnumDyeColor color;

	public @Nullable EnumDyeColor getColor() {
		return color;
	}

	public void setColor(EnumDyeColor color) {
		this.color = color;
	}

	public static MinecartDyeable get(EntityMinecart entity) {
		return entity.getCapability(CharsetTransportDyeableMinecarts.MINECART_DYEABLE, null);
	}

	public static class Storage implements Capability.IStorage<MinecartDyeable> {
		@Override
		public NBTBase writeNBT(Capability<MinecartDyeable> capability, MinecartDyeable instance, EnumFacing side) {
			if (instance != null) {
				NBTTagCompound compound = new NBTTagCompound();
				if (instance.color != null) {
					compound.setInteger("id", instance.color.getMetadata());
				}
				return compound;
			} else {
				return null;
			}
		}

		@Override
		public void readNBT(Capability<MinecartDyeable> capability, MinecartDyeable instance, EnumFacing side, NBTBase nbt) {
			if (nbt instanceof NBTTagCompound && instance != null) {
				NBTTagCompound compound = (NBTTagCompound) nbt;
				instance.color = null;
				if (compound.hasKey("id")) {
					instance.setColor(EnumDyeColor.byMetadata(compound.getInteger("id")));
				}
			}
		}
	}
}
