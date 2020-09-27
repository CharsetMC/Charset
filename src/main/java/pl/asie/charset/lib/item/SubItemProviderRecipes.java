/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

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

public abstract class SubItemProviderRecipes extends SubItemProviderSets {
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
                            if (stacks != null && !stacks.isEmpty()) list.add(stacks);
                        }
                    }
                } else {
                    ItemStack s = recipe.getRecipeOutput();
                    if (stackSet.add(s)) {
                        List<ItemStack> stacks = createSetFor(s);
                        if (stacks != null && !stacks.isEmpty()) list.add(stacks);
                    }
                }
            }
        }

        return list;
    }
}
