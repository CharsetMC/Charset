package pl.asie.charset.lib.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public final class RecipeUtils {
    private RecipeUtils() {

    }

    public static InventoryCrafting getCraftingInventory(int width, int height) {
        return new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, width, height);
    }

    public static IRecipe findMatchingRecipe(InventoryCrafting crafting, World world) {
        for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            if (recipe.matches(crafting, world)) {
                return recipe;
            }
        }

        return null;
    }
}
