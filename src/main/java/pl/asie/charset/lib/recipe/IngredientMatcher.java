/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.lib.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IngredientMatcher {
    public interface Container {
        Ingredient getIngredient(char c);
    }

    private final Container container;
    private final Map<Ingredient, ItemStack> matchedStacks = new HashMap<>();

    public IngredientMatcher(Container container) {
        this.container = container;
    }

    public Collection<Ingredient> getMatchedIngredients() {
        return matchedStacks.keySet();
    }

    public Ingredient getIngredient(char c) {
        return container.getIngredient(c);
    }

    public ItemStack getStack(Ingredient i) {
        return matchedStacks.getOrDefault(i, ItemStack.EMPTY);
    }

    public boolean add(ItemStack stack, Ingredient ingredient) {
        if (ingredient == Ingredient.EMPTY) {
            return stack.isEmpty();
        } else {
            boolean match = false;
            if (ingredient instanceof IngredientCharset) {
                IngredientCharset ic = (IngredientCharset) ingredient;
                if (((IngredientCharset) ingredient).apply(this, stack)) {
                    if (matchedStacks.containsKey(ingredient)) {
                        if (!ic.areItemStacksMatched(matchedStacks.get(ingredient), stack)) {
                            return false;
                        }
                    }

                    match = true;
                }
            } else {
                match = ingredient.apply(stack);
            }

            if (match) {
                matchedStacks.put(ingredient, stack);
                return true;
            } else {
                return false;
            }
        }
    }

    public ItemStack apply(ItemStack stack) {
        boolean applied = false;

        for (Map.Entry<Ingredient, ItemStack> entry : matchedStacks.entrySet()) {
            if (entry.getKey() instanceof IngredientCharset) {
                if (!applied) {
                    applied = true;
                    stack = stack.copy();
                }

                ((IngredientCharset) entry.getKey()).applyToStack(stack, entry.getValue());
            }
        }

        return stack;
    }
}
