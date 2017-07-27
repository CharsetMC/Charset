package pl.asie.charset.lib.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

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

    public static InventoryCrafting getCraftingInventory(int width, int height, ItemStack... stacks) {
        InventoryCrafting crafting = getCraftingInventory(width, height);
        for (int i = 0; i < Math.min(width * height, stacks.length); i++) {
            crafting.setInventorySlotContents(i, (stacks[i] == null || stacks[i].isEmpty()) ? ItemStack.EMPTY : stacks[i].copy());
        }
        return crafting;
    }

    public static ItemStack getCraftingResult(World world, int width, int height, ItemStack... stacks) {
        return getCraftingResult(world, getCraftingInventory(width, height, stacks));
    }

    public static ItemStack getCraftingResult(World world, InventoryCrafting crafting) {
        IRecipe recipe = findMatchingRecipe(crafting, world);
        if (recipe != null) {
            return recipe.getCraftingResult(crafting);
        }

        return ItemStack.EMPTY;
    }

    public static IRecipe findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn) {
        int width = craftMatrix.getWidth();
        int height = craftMatrix.getHeight();
        for (IRecipe irecipe : ForgeRegistries.RECIPES) {
            if (irecipe.canFit(width, height) && irecipe.matches(craftMatrix, worldIn)) {
                return irecipe;
            }
        }

        return null;
    }
}
