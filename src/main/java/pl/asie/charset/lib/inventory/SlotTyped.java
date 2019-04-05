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

package pl.asie.charset.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class SlotTyped extends SlotItemHandler {
	private Predicate<ItemStack>[] allowedTypes;
	
	public SlotTyped(IItemHandler handler, int par2, int par3, int par4, Predicate<ItemStack>... allowedTypes) {
		super(handler, par2, par3, par4);
		this.allowedTypes = allowedTypes;
	}

	@Override
    public boolean isItemValid(ItemStack stack) {
		for (Predicate<ItemStack> o: allowedTypes) {
        	if (o.test(stack)) {
		        return true;
	        }
        }
        return false;
    }
}
