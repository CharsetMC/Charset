package pl.asie.charset.module.storage.locks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import pl.asie.charset.lib.ui.ContainerBase;
import pl.asie.charset.lib.ui.SlotBlocked;

public class ContainerKeyring extends ContainerBase {
    private final InventoryPlayer inventoryPlayer;
    private final int heldPos;
    private ItemStack held;

    public ContainerKeyring(InventoryPlayer inventoryPlayer) {
        super(inventoryPlayer);
        this.inventoryPlayer = inventoryPlayer;
        heldPos = inventoryPlayer.currentItem;
        held = inventoryPlayer.getStackInSlot(heldPos);
        bindPlayerInventory(inventoryPlayer, 8, 50);

        ItemStack held = inventoryPlayer.getStackInSlot(heldPos);
        IItemHandler handler = held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotItemHandler(handler, i, 8 + (18 * i), 18));
        }

        detectAndSendChanges();
    }

    @Override
    protected Slot addSlotToContainer(Slot slotIn) {
        if (slotIn.isHere(inventoryPlayer, heldPos)) {
            Slot newSlot = new SlotBlocked(inventoryPlayer, slotIn.getSlotIndex(), slotIn.xPos, slotIn.yPos);
            return super.addSlotToContainer(newSlot);
        } else {
            return super.addSlotToContainer(slotIn);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Slot slot : this.SLOTS_PLAYER) {
            if (slot.isHere(inventoryPlayer, heldPos)) {
                ItemStack newHeld = slot.getStack();
                if (ItemStack.areItemStacksEqual(held, newHeld)) return;

                System.out.println("a");

                held = newHeld;
                for (int j = 0; j < this.listeners.size(); ++j) {
                    this.listeners.get(j).sendSlotContents(this, slot.slotNumber, held);
                }
            }
        }
    }
}
