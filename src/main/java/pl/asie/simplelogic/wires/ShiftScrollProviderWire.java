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

package pl.asie.simplelogic.wires;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import pl.asie.charset.lib.handlers.ShiftScrollHandler;

import java.util.Collection;
import java.util.List;

public class ShiftScrollProviderWire implements ShiftScrollHandler.Provider {
	private final Collection<Item> wires;
	private final boolean freestanding;

	public ShiftScrollProviderWire(Collection<Item> wires, boolean freestanding) {
		this.wires = wires;
		this.freestanding = freestanding;
	}

	@Override
	public boolean matches(ItemStack stack) {
		return wires.contains(stack.getItem()) && stack.getMetadata() == (freestanding ? 1 : 0);
	}

	@Override
	public void addAllMatching(NonNullList<ItemStack> list) {
		for (Item i : wires) {
			list.add(new ItemStack(i, 1, freestanding ? 1 : 0));
		}
	}
}
