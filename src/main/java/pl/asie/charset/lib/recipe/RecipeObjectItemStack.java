package pl.asie.charset.lib.recipe;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeObjectItemStack implements IRecipeObject {
    private final ItemStack target;

    public RecipeObjectItemStack(Block block) {
        target = new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE);
    }

    public RecipeObjectItemStack(Item item) {
        target = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
    }

    public RecipeObjectItemStack(ItemStack stack) {
        target = stack;
    }

    @Override
    public boolean test(ItemStack stack) {
        return OreDictionary.itemMatches(target, stack, false);
    }

    @Override
    public Object preview() {
        return target;
    }
}
