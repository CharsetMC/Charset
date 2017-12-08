package pl.asie.charset.lib.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public interface IRecipeResultBuilder extends IRecipeView {
    ItemStack getStack(Ingredient i);
}
