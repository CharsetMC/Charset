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

package pl.asie.charset.module.storage.locks.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.storage.IBarrel;

public class ReadOnlyBarrel implements IBarrel {
	private final IBarrel parent;

	public ReadOnlyBarrel(IBarrel parent) {
		this.parent = parent;
	}

	@Override
	public int getItemCount() {
		return parent.getItemCount();
	}

	@Override
	public int getMaxItemCount() {
		return parent.getMaxItemCount();
	}

	@Override
	public boolean containsUpgrade(String upgradeName) {
		return parent.containsUpgrade(upgradeName);
	}

	@Override
	public boolean shouldExtractFromSide(EnumFacing side) {
		return parent.shouldExtractFromSide(side);
	}

	@Override
	public boolean shouldInsertToSide(EnumFacing side) {
		return parent.shouldInsertToSide(side);
	}

	@Override
	public ItemStack extractItem(int maxCount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(ItemStack stack, boolean simulate) {
		return stack;
	}
}
