/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.misc;

import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

			if (entityItem == null || entityItem.getEntityItem().isEmpty()) {
				continue;
			}

			ItemStack stack = entityItem.getEntityItem();
			if (itemKeepPredicate.apply(stack)) {
				itemIterator.remove();

				event.getEntityPlayer().inventory.addItemStackToInventory(stack);
			}
		}
	}
}
