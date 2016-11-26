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

package pl.asie.charset.lib.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import pl.asie.charset.lib.ModCharsetLib;

public abstract class ContainerBase extends Container {
	private final int containerSize;
	private final IInventory inventory;

	public ContainerBase(IInventory inventory, InventoryPlayer inventoryPlayer) {
		this.inventory = inventory;

		if (inventory != null) {
			this.containerSize = inventory.getSizeInventory();
			this.inventory.openInventory(inventoryPlayer.player);
		} else {
			this.containerSize = 0;
		}
	}

	public int getSize() {
		return containerSize;
	}

	public IInventory getInventoryObject() {
		return inventory;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.inventory.isUsableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		if (inventory == null) {
			return null;
		}

		Slot slotObject = inventorySlots.get(slot);
		if (slotObject != null && slotObject.getHasStack()) {
			tryTransferStackInSlot(slotObject, slotObject.inventory == this.inventory);
			if (!ModCharsetLib.proxy.isClient()) {
				detectAndSendChanges();
			}
		}
		return null;
	}

	// From OpenComputers, looks like a good implementation
	protected void tryTransferStackInSlot(Slot from, boolean intoPlayerInventory) {
		ItemStack fromStack = from.getStack();
		boolean somethingChanged = false;

		int step = intoPlayerInventory ? -1 : 1;
		int begin = intoPlayerInventory ? (inventorySlots.size() - 1) : 0;
		int end = intoPlayerInventory ? 0 : inventorySlots.size() - 1;

		if (fromStack.getMaxStackSize() > 1) {
			for (int i = begin; i * step <= end; i += step) {
				if (i >= 0 && i < inventorySlots.size() && from.getHasStack() && from.getStack().getCount() > 0) {
					Slot intoSlot = inventorySlots.get(i);
					if (intoSlot.inventory != from.inventory && intoSlot.getHasStack()) {
						ItemStack intoStack = intoSlot.getStack();
						boolean itemsAreEqual = fromStack.isItemEqual(intoStack) && ItemStack.areItemStackTagsEqual(fromStack, intoStack);
						int maxStackSize = Math.min(fromStack.getMaxStackSize(), intoSlot.getSlotStackLimit());
						boolean slotHasCapacity = intoStack.getCount() < maxStackSize;
						if (itemsAreEqual && slotHasCapacity) {
							int itemsMoved = Math.min(maxStackSize - intoStack.getCount(), fromStack.getCount());
							if (itemsMoved > 0) {
								intoStack.grow(from.decrStackSize(itemsMoved).getCount());
								intoSlot.onSlotChanged();
								somethingChanged = true;
							}
						}
					}
				}
			}
		}

		for (int i = begin; i * step <= end; i += step) {
			if (i >= 0 && i < inventorySlots.size() && from.getHasStack() && from.getStack().getCount() > 0) {
				Slot intoSlot = inventorySlots.get(i);
				if (intoSlot.inventory != from.inventory && !intoSlot.getHasStack() && intoSlot.isItemValid(fromStack)) {
					int maxStackSize = Math.min(fromStack.getMaxStackSize(), intoSlot.getSlotStackLimit());
					int itemsMoved = Math.min(maxStackSize, fromStack.getCount());
					intoSlot.putStack(from.decrStackSize(itemsMoved));
					somethingChanged = true;
				}
			}
		}

		if (somethingChanged) {
			from.onSlotChanged();
		}
	}

	public void bindPlayerInventory(InventoryPlayer inventoryPlayer, int startX, int startY) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						startX + j * 18, startY + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, startX + i * 18, startY + 58));
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.inventory.closeInventory(player);
	}
}
