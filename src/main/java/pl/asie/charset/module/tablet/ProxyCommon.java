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

package pl.asie.charset.module.tablet;

import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ProxyCommon {
	public void init() {

	}

	public void openTabletItemStack(World world, EntityPlayer player, ItemStack stack) {

	}

	public void onTabletRightClick(World world, EntityPlayer player, EnumHand hand) {

	}

	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.EntityInteract event) {
		if (event.getEntityPlayer().isSneaking() && event.getHand() == EnumHand.MAIN_HAND && event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemTablet) {
			event.setCanceled(true);
			onTabletRightClick(event.getWorld(), event.getEntityPlayer(), EnumHand.MAIN_HAND);
		}
	}

	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntityPlayer().isSneaking() && event.getHand() == EnumHand.MAIN_HAND && event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemTablet) {
			event.setCanceled(true);
			onTabletRightClick(event.getWorld(), event.getEntityPlayer(), EnumHand.MAIN_HAND);
		}
	}
}
