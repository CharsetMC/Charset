package pl.asie.charset.lib.recipe;

import net.minecraft.item.ItemStack;

public interface IRecipeObject {
    boolean matches(ItemStack stack);
}
