package pl.asie.charset.api.storage;

import net.minecraft.item.ItemStack;

public interface IKeyItem {
    boolean canUnlock(String lock, ItemStack stack);
}
