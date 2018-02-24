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

package pl.asie.charset.module.crafting.compression.grid;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class GridEntry {
	private final World world;
	private final BlockPos pos;

	public GridEntry(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}

	public World getWorld() {
		return world;
	}

	public BlockPos getPos() {
		return pos;
	}

	public IBlockState getState() {
		return world.getBlockState(pos);
	}

	public TileEntity getTileEntity() {
		return world.getTileEntity(pos);
	}

	public abstract boolean isInvalid();
	public abstract ItemStack getCraftingStack();
	public abstract ItemStack mergeRemainingItem(ItemStack target, boolean simulate);
}
