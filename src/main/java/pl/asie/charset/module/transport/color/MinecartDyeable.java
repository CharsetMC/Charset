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

package pl.asie.charset.module.transport.color;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.ColorspaceUtils;

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
