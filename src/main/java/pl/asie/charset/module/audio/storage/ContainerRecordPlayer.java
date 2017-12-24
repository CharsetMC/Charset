/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.audio.storage;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.SlotItemHandler;
import pl.asie.charset.lib.ui.ContainerBase;

public class ContainerRecordPlayer extends ContainerBase {
	protected final TileRecordPlayer owner;

	public ContainerRecordPlayer(TileRecordPlayer owner, InventoryPlayer inventoryPlayer) {
		super(inventoryPlayer);
		this.owner = owner;
		this.addSlotToContainer(new SlotItemHandler(owner.getHandler(), 0, 80, 34));
		this.bindPlayerInventory(inventoryPlayer, 8, 84);
	}
}
