package pl.asie.charset.lib.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.lib.utils.ItemStackHashSet;

import javax.annotation.Nullable;
import java.util.*;

public abstract class SubItemProviderRecipes extends SubItemProviderSets {
    @Nullable
    protected Collection<ItemStack> createSetFor(ItemStack stack) {
        ItemStack newStack = stack.copy();
        newStack.setCount(1);
        return Collections.singleton(newStack);
    }

    protected abstract Item getItem();

    @Override
    protected List<Collection<ItemStack>> createItemSets() {
        List<Collection<ItemStack>> list = new ArrayList<>();
        Item item = getItem();
        ItemStackHashSet stackSet = new ItemStackHashSet(false, true, true);

        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (!recipe.getRecipeOutput().isEmpty() && recipe.getRecipeOutput().getItem() == item) {
                if (recipe instanceof RecipeCharset) {
                    for (ItemStack s : ((RecipeCharset) recipe).getAllRecipeOutputs()) {
                        if (stackSet.add(s)) {
                            Collection<ItemStack> stacks = createSetFor(s);
                            if (stacks != null && stacks.size() > 0) list.add(stacks);
                        }
                    }
                } else {
                    ItemStack s = recipe.getRecipeOutput();
                    if (stackSet.add(s)) {
                        Collection<ItemStack> stacks = createSetFor(s);
                        if (stacks != null && stacks.size() > 0) list.add(stacks);
                    }
                }
            }
        }

        return list;
    }
}
