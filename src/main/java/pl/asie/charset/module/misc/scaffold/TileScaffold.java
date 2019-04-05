/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.misc.scaffold;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.UnlistedPropertyGeneric;

import javax.annotation.Nullable;

public class TileScaffold extends TileBase {
	public static final UnlistedPropertyGeneric<ScaffoldCacheInfo> PROPERTY = new UnlistedPropertyGeneric<>("tile", ScaffoldCacheInfo.class);
	private ItemMaterial plank;

	public TileScaffold() {
		plank = getPlankFromNBT(null);
	}

	public ItemMaterial getPlank() {
		return plank;
	}

	public static ItemMaterial getPlankFromNBT(NBTTagCompound compound) {
		return ItemMaterialRegistry.INSTANCE.getMaterial(compound, "plank", "plank");
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		plank = getPlankFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		plank.writeToNBT(compound, "plank");
		return compound;
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		loadFromStack(stack);
	}

	public void loadFromStack(ItemStack stack) {
		plank = getPlankFromNBT(stack.getTagCompound());
	}

	@Override
	public ItemStack getDroppedBlock(IBlockState state) {
		ItemStack stack = new ItemStack(state.getBlock());
		stack.setTagCompound(writeNBTData(new NBTTagCompound(), false));
		return stack;
	}
}
