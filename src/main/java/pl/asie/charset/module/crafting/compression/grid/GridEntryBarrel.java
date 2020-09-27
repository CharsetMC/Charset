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

package pl.asie.charset.module.crafting.compression.grid;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.module.storage.barrels.BarrelUpgrade;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;

import java.util.Optional;

public class GridEntryBarrel extends GridEntry {
	private final TileEntityDayBarrel barrel;
	private final Orientation orientation;

	public GridEntryBarrel(TileEntityDayBarrel barrel) {
		super(barrel.getWorld(), barrel.getPos());
		this.barrel = barrel;
		this.orientation = barrel.getOrientation();
	}

	@Override
	public ItemStack getCraftingStack() {
		ItemStack stack = barrel.getItemUnsafe();
		if (!stack.isEmpty()) {
			boolean copied = false;
			if (barrel.getUpgrades().contains(BarrelUpgrade.STICKY)) {
				stack = stack.copy();
				stack.shrink(1);
				copied = true;
			}

			if (stack.getCount() > 1) {
				if (!copied) {
					stack = stack.copy();
					copied = true;
				}
				stack.setCount(1);
			}
		}

		return stack;
	}

	@Override
	public ItemStack mergeRemainingItem(ItemStack target, boolean simulate) {
		ItemStack source = barrel.getItemUnsafe();
		ItemStack sourceOrig = source;

		if (!source.isEmpty() && !barrel.getUpgrades().contains(BarrelUpgrade.INFINITE)) {
			sourceOrig = source.copy();
			if (!simulate) {
				source.shrink(1);
				barrel.setItem(source);
			}
		}

		if (target.isEmpty()) {
			return ItemStack.EMPTY;
		} else if (ItemUtils.canMerge(sourceOrig, target)) {
			int maxGrow = Math.min(barrel.getMaxItemCount() - source.getCount(), target.getCount());
			if (maxGrow > 0) {
				if (!simulate) {
					source.grow(maxGrow);
					barrel.setItem(source);
				}

				if (maxGrow == target.getCount()) {
					return ItemStack.EMPTY;
				} else {
					ItemStack targetCopy = target.copy();
					targetCopy.shrink(maxGrow);
					return targetCopy;
				}
			} else {
				return target;
			}
		} else {
			return target;
		}
	}

	@Override
	public boolean isInvalid() {
		return barrel.isInvalid() || barrel.getOrientation() != orientation;
	}

	@Override
	public TileEntity getTileEntity() {
		return barrel;
	}
}
