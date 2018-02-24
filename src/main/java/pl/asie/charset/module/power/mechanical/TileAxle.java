/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.power.mechanical;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.module.power.CharsetPower;

import javax.annotation.Nullable;

public class TileAxle extends TileBase {
	protected ItemMaterial material = ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank");
	protected boolean rendered;

	private boolean loadMaterialFromNBT(NBTTagCompound compound) {
		ItemMaterial nm = ItemMaterialRegistry.INSTANCE.getMaterial(compound, "material");
		if (nm != null && nm != material) {
			material = nm;
			return true;
		} else {
			return false;
		}
	}

	private void saveMaterialToNBT(NBTTagCompound compound) {
		getMaterial().writeToNBT(compound, "material");
	}

	@Override
	public ItemStack getDroppedBlock(IBlockState state) {
		ItemStack stack = new ItemStack(CharsetPower.itemAxle, 1, 0);
		saveMaterialToNBT(ItemUtils.getTagCompound(stack, true));
		return stack;
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		loadMaterialFromNBT(stack.getTagCompound());
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		boolean r = loadMaterialFromNBT(compound);
		if (/*(r || !rendered) && */isClient) {
			markBlockForRenderUpdate();
			rendered = true;
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		saveMaterialToNBT(compound);
		return compound;
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public ItemMaterial getMaterial() {
		if (material == null) {
			material = ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank");
		}
		return material;
	}
}
