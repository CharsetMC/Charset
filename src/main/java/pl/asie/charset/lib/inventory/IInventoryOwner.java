package pl.asie.charset.lib.inventory;

import net.minecraft.inventory.IInventory;

public interface IInventoryOwner {
    void onInventoryChanged(IInventory inventory);
}
