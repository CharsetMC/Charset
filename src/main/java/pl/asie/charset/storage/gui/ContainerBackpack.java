package pl.asie.charset.storage.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import pl.asie.charset.lib.container.ContainerBase;

public class ContainerBackpack extends ContainerBase {
    public ContainerBackpack(IInventory inventory, InventoryPlayer inventoryPlayer) {
        super(inventory, inventoryPlayer);

        bindPlayerInventory(inventoryPlayer, 8, 85);

        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }
    }
}
