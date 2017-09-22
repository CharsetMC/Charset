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

package pl.asie.charset.storage.backpack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.storage.ModCharsetStorage;

public class HandlerBackpack {
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntityPlayer().getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) == null
				&& event.getEntityPlayer().isSneaking()
				&& event.getFace() == EnumFacing.UP && !event.getWorld().isRemote) {
			ItemStack backpack = ItemBackpack.getBackpack(event.getEntityPlayer());
			if (backpack != null) {
				IBlockState sourceBlock = event.getWorld().getBlockState(event.getPos());
				if (sourceBlock.getBlock().isSideSolid(sourceBlock, event.getWorld(), event.getPos(), event.getFace())) {
					if (backpack.getItem().onItemUse(backpack, event.getEntityPlayer(), event.getWorld(), event.getPos(), EnumHand.MAIN_HAND, event.getFace(), 0, 0, 0) == EnumActionResult.SUCCESS) {
						event.setCanceled(true);
						event.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.CHEST, null);
						event.getEntityPlayer().inventoryContainer.detectAndSendChanges();
						TileEntity tile = event.getWorld().getTileEntity(event.getPos().up());
						if (tile instanceof TileBackpack) {
							((TileBackpack) tile).readFromItemStack(backpack);
						} else {
							ModCharsetStorage.logger.error("Something went wrong with placing backpack at " + event.getPos().toString() + "! Please report!");
						}
					}
				}
			}
		}
	}
}
