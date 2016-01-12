package pl.asie.charset.lib.recipe;

import net.minecraft.item.ItemStack;

public interface IDyeableItem {
    int getColor(ItemStack stack);
    boolean hasColor(ItemStack stack);
    void setColor(ItemStack stack, int color);
}
