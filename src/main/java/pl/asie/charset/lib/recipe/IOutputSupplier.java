package pl.asie.charset.lib.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public interface IOutputSupplier {
    ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv);
    ItemStack getDefaultOutput();
}
