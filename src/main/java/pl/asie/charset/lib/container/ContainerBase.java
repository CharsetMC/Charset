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
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ContainerBase extends Container {
	protected final Collection<Slot> SLOTS_PLAYER = new ArrayList<>(36);
	protected final Collection<Slot> SLOTS_INVENTORY = new ArrayList<>();
	private final IContainerHandler containerHandler;

	public ContainerBase(InventoryPlayer inventoryPlayer) {
		this(inventoryPlayer, null);
	}

	public ContainerBase(InventoryPlayer inventoryPlayer, IContainerHandler listener) {
		this.containerHandler = listener;
		if (listener != null) {
			listener.onOpenedBy(inventoryPlayer.player);
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return containerHandler != null ? containerHandler.isUsableByPlayer(player) : true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		Slot slotObject = inventorySlots.get(slot);
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stack = tryTransferStackInSlot(player, slotObject, slotObject.inventory == player.inventory ? SLOTS_INVENTORY : SLOTS_PLAYER);
			if (!ModCharsetLib.proxy.isClient()) {
				detectAndSendChanges();
			}
			return stack;
		} else {
			return slotObject.getStack();
		}
	}

	private boolean tryInsertStackToSlot(EntityPlayer player, Slot from, Slot to) {
		if (!to.getHasStack() || ItemUtils.canMerge(from.getStack(), to.getStack())) {
			ItemStack fromStack = from.getStack();
			ItemStack toStack = to.getStack();

			int maxSize = Math.min(toStack.getMaxStackSize(), to.getSlotStackLimit());
			if (toStack.isEmpty()) {
				int amount = Math.min(fromStack.getCount(), maxSize);

				if (amount > 0) {
					to.putStack(fromStack.splitStack(amount));
					to.onSlotChanged();

					return true;
				}
			} else {
				int amount = Math.min(maxSize - toStack.getCount(), fromStack.getCount());

				if (amount > 0) {
					fromStack.shrink(amount);
					toStack.grow(amount);
					to.onSlotChanged();

					return true;
				}
			}
		}

		return false;
	}

	// TODO: BUGTEST ME
	protected ItemStack tryTransferStackInSlot(EntityPlayer player, Slot from, Collection<Slot> targets) {
		ItemStack fromStack = from.getStack();
		Collection<Slot> targetsValidEmpty = new ArrayList<>(targets.size());
		boolean dirty = false;

		if (!from.getHasStack())
			return from.getStack();

		// Pass 1: Merge
		for (Slot to : targets) {
			if (to.isItemValid(fromStack)) {
				if (to.getHasStack()) {
					dirty |= tryInsertStackToSlot(player, from, to);

					if (!from.getHasStack())
						break;
				} else {
					targetsValidEmpty.add(to);
				}
			}
		}

		// Pass 2: Place
		if (from.getHasStack()) {
			for (Slot to : targetsValidEmpty) {
				dirty |= tryInsertStackToSlot(player, from, to);

				if (!from.getHasStack())
					break;
			}
		}

		if (dirty) {
			from.onSlotChanged();
		}

		return fromStack;
	}

	public void bindPlayerInventory(InventoryPlayer inventoryPlayer, int startX, int startY) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addPlayerSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						startX + j * 18, startY + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			addPlayerSlotToContainer(new Slot(inventoryPlayer, i, startX + i * 18, startY + 58));
		}
	}

	@Override
	protected Slot addSlotToContainer(Slot slotIn) {
		slotIn = super.addSlotToContainer(slotIn);
		SLOTS_INVENTORY.add(slotIn);
		return slotIn;
	}

	protected Slot addPlayerSlotToContainer(Slot slot) {
		slot = super.addSlotToContainer(slot);
		SLOTS_PLAYER.add(slot);
		return slot;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (containerHandler != null) {
			this.containerHandler.onClosedBy(player);
		}
	}
}
