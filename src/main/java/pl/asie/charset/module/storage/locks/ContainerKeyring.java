package pl.asie.charset.module.storage.locks;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import pl.asie.charset.lib.ui.ContainerBase;

public class ContainerKeyring extends ContainerBase {
    public ContainerKeyring(InventoryPlayer inventoryPlayer) {
        super(inventoryPlayer);
        bindPlayerInventory(inventoryPlayer, 8, 50);

        ItemStack held = inventoryPlayer.getCurrentItem();
        IItemHandler handler = held.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotItemHandler(handler, i, 8 + (18 * i), 18));
        }

        detectAndSendChanges();
    }
}
