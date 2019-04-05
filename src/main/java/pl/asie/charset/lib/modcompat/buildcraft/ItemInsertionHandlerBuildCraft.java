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

package pl.asie.charset.lib.modcompat.buildcraft;

import buildcraft.api.transport.IInjectable;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.lib.IItemInsertionHandler;

public class ItemInsertionHandlerBuildCraft implements IItemInsertionHandler {
	private final IInjectable parent;
	private final EnumFacing side;

	public ItemInsertionHandlerBuildCraft(IInjectable parent, EnumFacing side) {
		this.parent = parent;
		this.side = side;
	}

	@Override
	public ItemStack insertItem(ItemStack stack, boolean simulate) {
		return insertItem(stack, null, simulate);
	}

	@Override
	public ItemStack insertItem(ItemStack stack, EnumDyeColor color, boolean simulate) {
		return parent.injectItem(stack, !simulate, side, color, 0);
	}
}
