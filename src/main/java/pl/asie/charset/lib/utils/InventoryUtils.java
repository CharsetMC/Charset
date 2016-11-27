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

package pl.asie.charset.lib.utils;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.Capabilities;

public final class InventoryUtils {
	private InventoryUtils() {

	}

	public static IItemInsertionHandler getItemInsertionHandler(ICapabilityProvider tile, EnumFacing facing) {
		if (tile == null) {
			return null;
		}

		if (tile.hasCapability(Capabilities.ITEM_INSERTION_HANDLER, facing)) {
			return tile.getCapability(Capabilities.ITEM_INSERTION_HANDLER, facing);
		} else {
			IItemHandler handler = getItemHandler(tile, facing);
			if (handler != null) {
				return new IItemInsertionHandler() {
					@Override
					public ItemStack insertItem(ItemStack stack, boolean simulate) {
						return ItemHandlerHelper.insertItem(handler, stack, simulate);
					}
				};
			}
		}

		return null;
	}

	public static IItemHandler getItemHandler(ICapabilityProvider tile, EnumFacing facing) {
		if (tile == null) {
			return null;
		}

		if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
			return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
		} else if (tile instanceof TileEntity) {
			if (tile instanceof ISidedInventory) {
				IItemHandler handler = new SidedInvWrapper((ISidedInventory) tile, facing);
				return handler.getSlots() > 0 ? handler : null;
			} else if (tile instanceof IInventory) {
				return new InvWrapper((IInventory) tile);
			}
		}

		return null;
	}
}
