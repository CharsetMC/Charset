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

package pl.asie.charset.lib.misc;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;

public class PlayerDeathHandler {
	private Predicate<ItemStack> itemKeepPredicate;

	public PlayerDeathHandler() {

	}

	public boolean hasPredicate() {
		return itemKeepPredicate != null;
	}

	public void addPredicate(Predicate<ItemStack> predicate) {
		if (itemKeepPredicate == null) {
			itemKeepPredicate = predicate;
		} else {
			itemKeepPredicate = Predicates.or(itemKeepPredicate, predicate);
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			for (int i = 0; i < Math.min(event.getEntityPlayer().inventory.getSizeInventory(), event.getOriginal().inventory.getSizeInventory()); i++) {
				ItemStack s = event.getOriginal().inventory.getStackInSlot(i);
				if (!s.isEmpty() && itemKeepPredicate.apply(s)) {
					event.getEntityPlayer().inventory.setInventorySlotContents(i, s);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(PlayerDropsEvent event) {
		Iterator<EntityItem> itemIterator = event.getDrops().iterator();

		while (itemIterator.hasNext()) {
			EntityItem entityItem = itemIterator.next();

			if (entityItem == null || entityItem.getItem().isEmpty()) {
				continue;
			}

			ItemStack stack = entityItem.getItem();
			if (itemKeepPredicate.apply(stack)) {
				itemIterator.remove();

				event.getEntityPlayer().inventory.addItemStackToInventory(stack);
			}
		}
	}
}
