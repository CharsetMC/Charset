package pl.asie.charset.module.storage.barrels;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import pl.asie.charset.lib.recipe.IOutputSupplier;
import pl.asie.charset.lib.recipe.IngredientMatcher;
import pl.asie.charset.lib.recipe.RecipeCharset;

public class BarrelCartOutputSupplier implements IOutputSupplier {
    @Override
    public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
        for (Ingredient i : matcher.getMatchedIngredients()) {
            ItemStack is = matcher.getStack(i);
            if (is.getItem() == CharsetStorageBarrels.barrelItem) {
                return CharsetStorageBarrels.barrelCartItem.makeBarrelCart(is);
            }
        }
        return new ItemStack(CharsetStorageBarrels.barrelCartItem);
    }

    @Override
    public ItemStack getDefaultOutput() {
        return new ItemStack(CharsetStorageBarrels.barrelCartItem);
    }
}
