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

package pl.asie.charset.module.storage.chests;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;
import pl.asie.charset.lib.ui.ContainerBase;

public class ContainerChestCharset extends ContainerBase {
	protected final TileEntityChestCharset tile;
	protected final int inventoryRows;

	public ContainerChestCharset(TileEntityChestCharset tile, InventoryPlayer inventoryPlayer) {
		super(inventoryPlayer, tile);
		this.tile = tile;
		this.inventoryRows = (tile.getSlots() + 8) / 9;

		int slot = 0;
		for (int y = 0; y < inventoryRows; y++) {
			for (int x = 0; x < 9; x++, slot++) {
				if (slot >= tile.getSlots()) {
					break;
				}

				addSlotToContainer(new SlotItemHandler(tile, slot, 8 + (x * 18), 18 + (y * 18)));
			}
		}
		bindPlayerInventory(inventoryPlayer, 8, 139 + ((inventoryRows - 6) * 18));
	}

	@Override
	public boolean isOwnerPresent() {
		return !tile.isInvalid();
	}
}
