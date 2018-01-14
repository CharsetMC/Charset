/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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
import java.util.HashMap;
import java.util.Map;

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
        return charset.getMatchingStacksCompressed();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IntList getValidItemStacksPacked() {
        return charset.getValidItemStacksPacked();
    }

    @Override
    protected void invalidate() {
        charset.invalidate();
    }

    @Override
    public boolean isSimple() {
        return false;
    }
}
