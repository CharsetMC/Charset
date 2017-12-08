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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.recipe.IRecipeResultBuilder;

import javax.annotation.Nullable;

public final class IngredientWrapper extends Ingredient {
    private static final IRecipeResultBuilder DEFAULT_BUILDER = new IRecipeResultBuilder() {
        @Override
        public Ingredient getIngredient(char c) {
            return null;
        }

        @Override
        public ItemStack getStack(Ingredient i) {
            return ItemStack.EMPTY;
        }
    };

    private final IngredientCharset charset;
    private IntList matchingStacksPacked;

    IngredientWrapper(IngredientCharset charset) {
        super(0);
        this.charset = charset;
    }

    public final IngredientCharset getIngredientCharset() {
        return charset;
    }

    @Override
    public boolean apply(@Nullable ItemStack stack) {
        return stack == null ? charset.matches(ItemStack.EMPTY, DEFAULT_BUILDER) : charset.matches(stack, DEFAULT_BUILDER);
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        ItemStack[][] stackArrays = charset.getMatchingStacks();
        int length = 0;
        for (ItemStack[] array : stackArrays) {
            length += array.length;
        }

        int i = 0;
        ItemStack[] stacks = new ItemStack[length];
        for (ItemStack[] array : stackArrays) {
            System.arraycopy(array, 0, stacks, i, array.length);
            i += array.length;
        }

        return stacks;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IntList getValidItemStacksPacked() {
        if(this.matchingStacksPacked == null) {
            ItemStack[] matchingStacks = getMatchingStacks();
            this.matchingStacksPacked = new IntArrayList(matchingStacks.length);
            ItemStack[] var1 = matchingStacks;
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                ItemStack itemstack = var1[var3];
                this.matchingStacksPacked.add(RecipeItemHelper.pack(itemstack));
            }

            this.matchingStacksPacked.sort(IntComparators.NATURAL_COMPARATOR);
        }

        return this.matchingStacksPacked;
    }

    @Override
    public boolean isSimple() {
        return false;
    }
}
