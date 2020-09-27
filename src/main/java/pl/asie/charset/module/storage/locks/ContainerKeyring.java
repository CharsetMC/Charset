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

package pl.asie.charset.module.storage.locks;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import pl.asie.charset.lib.inventory.ContainerBase;
import pl.asie.charset.lib.inventory.SlotBlocked;

public class ContainerKeyring extends ContainerBase {
    protected ItemStack held;
    private final InventoryPlayer inventoryPlayer;
    private final int heldPos;
    private Slot heldSlot;

    public ContainerKeyring(InventoryPlayer inventoryPlayer) {
        super(inventoryPlayer);
        this.inventoryPlayer = inventoryPlayer;
        heldPos = inventoryPlayer.currentItem;
        held = inventoryPlayer.getStackInSlot(heldPos);
        bindPlayerInventory(inventoryPlayer, 8, 50);

        IItemHandler handler = held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotItemHandler(handler, i, 8 + (18 * i), 18));
        }

        detectAndSendChanges();
    }

    @Override
    public boolean isOwnerPresent() {
        ItemStack heldNow = inventoryPlayer.getStackInSlot(heldPos);
        return ItemStack.areItemStacksEqual(held, heldNow);
    }

    @Override
    protected Slot addSlotToContainer(Slot slotIn) {
        if (slotIn.isHere(inventoryPlayer, heldPos)) {
            heldSlot = new SlotBlocked(inventoryPlayer, slotIn.getSlotIndex(), slotIn.xPos, slotIn.yPos);
            return super.addSlotToContainer(heldSlot);
        } else {
            return super.addSlotToContainer(slotIn);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        ItemStack newHeld = heldSlot.getStack();
        if (ItemStack.areItemStacksEqual(held, newHeld)) return;

        held = newHeld.copy();
        for (int j = 0; j < this.listeners.size(); ++j) {
            this.listeners.get(j).sendSlotContents(this, heldSlot.slotNumber, held);
        }
    }
}
