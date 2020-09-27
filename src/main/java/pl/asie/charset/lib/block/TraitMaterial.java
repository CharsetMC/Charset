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

package pl.asie.charset.lib.block;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nullable;

public class TraitMaterial extends Trait implements ITraitItemAppendable {
	private final String name;
	private final ItemMaterial defaultMaterial;
	private ItemMaterial material;

	public TraitMaterial(String name, ItemMaterial material) {
		this.name = name;
		this.defaultMaterial = material;
		this.material = material;
	}

	public ItemMaterial getMaterial() {
		return material;
	}

	@Override
	public ItemStack saveToStack(ItemStack stack) {
		material.writeToNBT(ItemUtils.getTagCompound(stack, true), name);
		return stack;
	}

	@Override
	public void loadFromStack(ItemStack stack) {
		if (stack.hasTagCompound()) {
			ItemMaterial newMaterial = ItemMaterialRegistry.INSTANCE.getMaterial(stack.getTagCompound(), name);
			if (newMaterial != null) {
				material = newMaterial;
			} else {
				material = defaultMaterial;
			}
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		ItemMaterial newMaterial = ItemMaterialRegistry.INSTANCE.getMaterial(compound, "material");
		if (newMaterial != null) {
			material = newMaterial;
		} else {
			material = defaultMaterial;
		}
	}

	@Override
	public NBTTagCompound writeNBTData(boolean isClient) {
		NBTTagCompound tag = new NBTTagCompound();
		material.writeToNBT(tag, "material");
		return tag;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return null;
	}
}
