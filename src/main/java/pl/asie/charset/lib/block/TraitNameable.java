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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nullable;

public class TraitNameable extends Trait implements ITraitItemAppendable {
	private ITextComponent component;

	public ITextComponent getName(ITextComponent fallback) {
		return component != null ? component : fallback;
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		if (compound.hasKey("nameLoc", Constants.NBT.TAG_STRING)) {
			component = new TextComponentTranslation(compound.getString("nameLoc"));
		} else if (compound.hasKey("name", Constants.NBT.TAG_STRING)) {
			component = new TextComponentString(compound.getString("name"));
		} else {
			component = null;
		}
	}

	@Override
	public NBTTagCompound writeNBTData(boolean isClient) {
		NBTTagCompound compound = new NBTTagCompound();
		if (component instanceof TextComponentTranslation) {
			compound.setString("nameLoc", ((TextComponentTranslation) component).getKey());
		} else if (component instanceof TextComponentString) {
			compound.setString("name", ((TextComponentString) component).getText());
		}
		return compound;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return null;
	}

	@Override
	public ItemStack saveToStack(ItemStack stack) {
		if (component instanceof TextComponentTranslation) {
			return stack.setTranslatableName(((TextComponentTranslation) component).getKey());
		} else if (component instanceof TextComponentString) {
			return stack.setStackDisplayName(((TextComponentString) component).getText());
		} else {
			return stack;
		}
	}

	@Override
	public void loadFromStack(ItemStack stack) {
		if (stack.hasDisplayName()) {
			component = new TextComponentString(stack.getDisplayName());
		} else {
			component = null;
		}
	}
}
