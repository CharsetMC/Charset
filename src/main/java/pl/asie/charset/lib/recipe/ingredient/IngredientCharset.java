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

package pl.asie.charset.lib.recipe.ingredient;

import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import pl.asie.charset.lib.recipe.IRecipeResultBuilder;
import pl.asie.charset.lib.recipe.IRecipeView;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Charset's replacement Ingredient class.
 *
 * Use IngredientCharset.wrap() to create a Minecraft-compatible Ingredient.
 * Use RecipeCharset (charset:shaped, charset:shapeless) to create IRecipes
 * which are capable of utilizing additional functionality in this class.
 * (The Ingredient wrapper will try to gracefully fall back when necessary.)
 */
public abstract class IngredientCharset {
    private static final Map<IngredientCharset, MatchingStacks> MATCHING_STACK_CACHE = new HashMap<>();

    static class MatchingStacks {
        private ItemStack[][] matchingStacks;
        private ItemStack[] matchingStacksCompressed;
        private IntList matchingStacksPacked;

        MatchingStacks(ItemStack[][] matchingStacks) {
            this.matchingStacks = matchingStacks;
        }

        ItemStack[] getMatchingStacksCompressed() {
            if (this.matchingStacksCompressed == null) {
                int i = 0;
                int length = 0;
                for (ItemStack[] array : matchingStacks) {
                    length += array.length;
                }
                ItemStack[] stacks = new ItemStack[length];
                for (ItemStack[] array : matchingStacks) {
                    System.arraycopy(array, 0, stacks, i, array.length);
                    i += array.length;
                }
                this.matchingStacksCompressed = stacks;
            }
            return matchingStacksCompressed;
        }

        IntList getValidItemStacksPacked() {
            if(this.matchingStacksPacked == null) {
                getMatchingStacksCompressed();

                this.matchingStacksPacked = new IntArrayList(matchingStacks.length);
                for(int i = 0; i < matchingStacksCompressed.length; i++) {
                    ItemStack itemstack = matchingStacksCompressed[i];
                    this.matchingStacksPacked.add(RecipeItemHelper.pack(itemstack));
                }

                this.matchingStacksPacked.sort(IntComparators.NATURAL_COMPARATOR);
            }

            return this.matchingStacksPacked;
        }
    }

    private static final TCharSet EMPTY_CHAR_SET = new TCharHashSet();

    public IngredientCharset() {
    }

    private MatchingStacks getMatchingStacksObj() {
        MatchingStacks ms = MATCHING_STACK_CACHE.get(this);
        if (ms == null) {
            ms = new MatchingStacks(createMatchingStacks());
            MATCHING_STACK_CACHE.put(this, ms);
        }
        return ms;
    }

    public final ItemStack[][] getMatchingStacks() {
        return getMatchingStacksObj().matchingStacks;
    }

    ItemStack[] getMatchingStacksCompressed() {
        return getMatchingStacksObj().getMatchingStacksCompressed();
    }

    IntList getValidItemStacksPacked() {
        return getMatchingStacksObj().getValidItemStacksPacked();
    }

    /**
     * Called when the Ingredient has been added to an IRecipe.
     * The intention is to allow looking up informations about
     * potential co-dependent Ingredients (see getDependencies()).
     *
     * @param view The recipe's view.
     */
    public void onAdded(IRecipeView view) {

    }

    /**
     * Are this ingredient's permutations distinct?
     *
     * This, among others, governs recipe preview behaviour in JEI
     * as well as whether two different stacks in two different recipe
     * slots under the same ingredient object can be used to craft
     * the item. (If true, they cannot - the stacks must all be mergeable.)
     */
    public boolean arePermutationsDistinct() {
        return false;
    }

    /**
     * Do these two ItemStacks match in the context of the same
     * recipe grid? It can be safely assumed that both already fulfill
     * IngredientCharset.matches().
     */
    public boolean matchSameGrid(ItemStack a, ItemStack b) {
        return !arePermutationsDistinct() || ItemUtils.canMerge(a, b);
    }

    /**
     * Does this ItemStack match in the context of this Ingredient?
     *
     * The addition here is the IRecipeResultBuilder, letting you look up
     * what exists in other ingredients. (This behaviour is only
     * safe for characters previously defined in getDependencies())
     */
    public abstract boolean matches(ItemStack stack, IRecipeResultBuilder builder);

    /**
     * Transform a given ItemStack based on the ItemStack fed to this ingredient.
     * Can be used to, for example, append NBT data based on the specific ItemStack
     * matched.
     *
     * @param stack The stack to be transformed.
     * @param source The matched stack in the recipe grid slot.
     * @param builder The current recipe result builder.
     * @return A transformation of the "stack" parameter.
     */
    public ItemStack transform(ItemStack stack, ItemStack source, IRecipeResultBuilder builder) {
        return stack;
    }

    /**
     * @return An array of arrays of matching ItemStacks, with one entry in the list
     * per possible permutation. If permutations are not distinct, the outer array
     * should have a length of 1!
     */
    protected abstract ItemStack[][] createMatchingStacks();

    /**
     * @return A set of characters which signify Ingredients this ingredient
     * wishes to look up the state for.
     */
    public TCharSet getDependencies() {
        return EMPTY_CHAR_SET;
    }

    /**
     * Magical wrapping logic!
     *
     * @param ingredient A Charset Ingredient.
     * @return A Minecraft-compatible Ingredient.
     */
    public static final Ingredient wrap(IngredientCharset ingredient) {
        return new IngredientWrapper(ingredient);
    }

    public void invalidate() {
        MATCHING_STACK_CACHE.remove(this);
    }
}
