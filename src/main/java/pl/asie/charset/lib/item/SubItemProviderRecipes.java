package pl.asie.charset.lib.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.lib.utils.ItemStackHashSet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SubItemProviderRecipes extends SubItemProviderSets {
    private final Supplier<Item> itemSupplier;
    private String group;

    public SubItemProviderRecipes(Supplier<Item> itemSupplier) {
        this(null, itemSupplier);
    }

    public SubItemProviderRecipes(String group, Supplier<Item> itemSupplier) {
        this.group = group;
        this.itemSupplier = itemSupplier;
    }

    @Nullable
    protected List<ItemStack> createSetFor(ItemStack stack) {
        ItemStack newStack = stack.copy();
        newStack.setCount(1);
        return Collections.singletonList(newStack);
    }

    @Override
    protected List<List<ItemStack>> createItemSets() {
        List<List<ItemStack>> list = new ArrayList<>();
        Item item = itemSupplier.get();

        if (group == null) {
            group = item.getRegistryName().toString();
        }

        ItemStackHashSet stackSet = new ItemStackHashSet(false, true, true);

        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if ((group == null || group.equals(recipe.getGroup())) && !recipe.getRecipeOutput().isEmpty() && recipe.getRecipeOutput().getItem() == item) {
                if (recipe instanceof RecipeCharset) {
                    for (ItemStack s : ((RecipeCharset) recipe).getAllRecipeOutputs()) {
                        if (stackSet.add(s)) {
                            List<ItemStack> stacks = createSetFor(s);
                            if (stacks != null && stacks.size() > 0) list.add(stacks);
                        }
                    }
                } else {
                    ItemStack s = recipe.getRecipeOutput();
                    if (stackSet.add(s)) {
                        List<ItemStack> stacks = createSetFor(s);
                        if (stacks != null && stacks.size() > 0) list.add(stacks);
                    }
                }
            }
        }

        return list;
    }
}
