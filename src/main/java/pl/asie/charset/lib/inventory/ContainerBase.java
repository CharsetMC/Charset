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

package pl.asie.charset.lib.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ContainerBase extends Container {
	protected final Collection<Slot> SLOTS_PLAYER = new ArrayList<>(36);
	protected final Collection<Slot> SLOTS_INVENTORY = new ArrayList<>();
	protected final EntityPlayer owner;
	private final IContainerHandler containerHandler;

	public ContainerBase(InventoryPlayer inventoryPlayer) {
		this(inventoryPlayer, null);
	}

	public ContainerBase(InventoryPlayer inventoryPlayer, IContainerHandler listener) {
		this.owner = inventoryPlayer.player;
		this.containerHandler = listener;
		if (listener != null) {
			listener.onOpenedBy(inventoryPlayer.player);
		}
	}

	public abstract boolean isOwnerPresent();

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return isOwnerPresent() && containerHandler != null ? containerHandler.isUsableByPlayer(player) : (owner == player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		Slot slotObject = inventorySlots.get(slot);
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stack = tryTransferStackInSlot(player, slotObject, slotObject.inventory == player.inventory ? SLOTS_INVENTORY : SLOTS_PLAYER);
			detectAndSendChanges();
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
					to.putStack(to.getStack());

					return true;
				}
			} else {
				int amount = Math.min(maxSize - toStack.getCount(), fromStack.getCount());

				if (amount > 0) {
					fromStack.shrink(amount);
					toStack.grow(amount);
					to.putStack(to.getStack());

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
			// Using putStack instead of onSlotChanged here,
			// as putStack calls the latter + IItemHandler's
			// onContentsChanged.
			from.putStack(from.getStack());
		}

		return fromStack;
	}

	int playerInventoryX = -1, playerInventoryY = -1;
	InventoryPlayer playerInventory = null;

	public void bindPlayerInventory(InventoryPlayer inventoryPlayer, int startX, int startY) {
		playerInventoryX = startX;
		playerInventoryY = startY;
		playerInventory = inventoryPlayer;
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
	protected Slot addSlotToContainer(Slot slotIn) {
		slotIn = super.addSlotToContainer(slotIn);
		if (slotIn.inventory instanceof InventoryPlayer) {
			SLOTS_PLAYER.add(slotIn);
		} else {
			SLOTS_INVENTORY.add(slotIn);
		}
		return slotIn;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (containerHandler != null) {
			this.containerHandler.onClosedBy(player);
		}
	}
}
