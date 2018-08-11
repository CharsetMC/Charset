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

package pl.asie.charset.lib;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.wires.CharsetLibWires;
import pl.asie.charset.lib.wires.ItemWire;
import pl.asie.charset.lib.wires.WireProvider;

public abstract class CreativeTabCharset extends CreativeTabs {
	public CreativeTabCharset(String label) {
		super(label);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list) {
		boolean addedWires = false;

		for (Item item : Item.REGISTRY) {
			if (item instanceof ItemWire) {
				if (!addedWires) {
					CharsetLibWires.getOrderedWireProviders().map(WireProvider::getItemWire)
							.filter((i) -> Item.REGISTRY.containsKey(i.getRegistryName()))
							.forEachOrdered((i) -> i.getSubItems(this, list));

					addedWires = true;
				}
			} else {
				item.getSubItems(this, list);
			}
		}
	}
}
