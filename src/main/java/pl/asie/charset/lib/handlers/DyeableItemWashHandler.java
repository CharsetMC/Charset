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

package pl.asie.charset.lib.handlers;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.api.lib.IDyeableItem;

public class DyeableItemWashHandler {
	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
		if (!event.getWorld().isRemote && !event.getEntityPlayer().isSneaking()) {
			ItemStack stack = event.getEntityPlayer().getHeldItem(event.getHand());
			if (!stack.isEmpty() && stack.hasCapability(Capabilities.DYEABLE_ITEM, null)) {
				IBlockState state = event.getWorld().getBlockState(event.getPos());
				IDyeableItem item = stack.getCapability(Capabilities.DYEABLE_ITEM, null);

				if (state.getBlock() instanceof BlockCauldron
						&& state.getPropertyKeys().contains(BlockCauldron.LEVEL)) {
					event.setCanceled(true);

					int level = state.getValue(BlockCauldron.LEVEL);
					boolean removed = false;
					if (level > 0) {
						for (int i = 0; i < item.getColorSlotCount(); i++) {
							if (item.hasColor(i) && item.removeColor(i)) {
								removed = true;
							}
						}
					}

					if (removed) {
							event.getWorld().setBlockState(event.getPos(), state.withProperty(BlockCauldron.LEVEL, level - 1));
							event.getEntityPlayer().addStat(StatList.ARMOR_CLEANED);
					}
				}
			}
		}
	}
}
