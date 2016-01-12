package pl.asie.charset.lib.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public abstract class RecipeBase implements IRecipe {
    @Override
    public int getRecipeSize()
    {
        return 10;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        ItemStack[] stacks = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < stacks.length; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            stacks[i] = net.minecraftforge.common.ForgeHooks.getContainerItem(stack);
        }

        return stacks;
    }
}
