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

package pl.asie.charset.lib.handlers;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.lib.IWashableItem;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.api.lib.IDyeableItem;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.Optional;

public class DyeableItemWashHandler {
	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
		if (!event.getWorld().isRemote && !event.getEntityPlayer().isSneaking()) {
			boolean removed = false;
			ItemStack stack = event.getEntityPlayer().getHeldItem(event.getHand());
			if (stack.isEmpty()) {
				return;
			}

			if (stack.hasCapability(Capabilities.DYEABLE_ITEM, null)) {
				IBlockState state = event.getWorld().getBlockState(event.getPos());
				IDyeableItem item = stack.getCapability(Capabilities.DYEABLE_ITEM, null);

				if (item != null && state.getBlock() instanceof BlockCauldron
						&& state.getPropertyKeys().contains(BlockCauldron.LEVEL)) {
					event.setCanceled(true);

					int level = state.getValue(BlockCauldron.LEVEL);
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
			} else if (stack.hasCapability(Capabilities.WASHABLE_ITEM, null)) {
				IBlockState state = event.getWorld().getBlockState(event.getPos());
				IWashableItem item = stack.getCapability(Capabilities.WASHABLE_ITEM, null);

				if (item != null && state.getBlock() instanceof BlockCauldron
						&& state.getPropertyKeys().contains(BlockCauldron.LEVEL)) {
					int level = state.getValue(BlockCauldron.LEVEL);
					if (level > 0) {
						event.setCanceled(true);
						Optional<ItemStack> result = item.wash(stack);

						if (result.isPresent()) {
							ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
							if (ItemUtils.canMerge(held, result.get()) && (held.getCount() + result.get().getCount()) <= result.get().getMaxStackSize()) {
								result.get().grow(held.getCount());
								event.getEntityPlayer().setHeldItem(event.getHand(), result.get());
							} else {
								ItemUtils.giveOrSpawnItemEntity(
										event.getEntityPlayer(),
										event.getWorld(),
										new Vec3d(event.getPos()).add(0.5, 1, 0.5),
										result.get(), 0, 0, 0, 0, true
								);
							}

							event.getWorld().setBlockState(event.getPos(), state.withProperty(BlockCauldron.LEVEL, level - 1));
							event.getEntityPlayer().addStat(StatList.ARMOR_CLEANED);
						}
					}
				}
			}
		}
	}
}
