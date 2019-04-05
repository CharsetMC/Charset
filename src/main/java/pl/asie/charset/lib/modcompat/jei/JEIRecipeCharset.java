/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.modcompat.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.recipe.RecipeCharset;

import java.util.Collections;

public class JEIRecipeCharset implements ICraftingRecipeWrapper {
    public static class Shapeless extends JEIRecipeCharset {
        public Shapeless(RecipeCharset recipe) {
            super(recipe);
        }
    }

    public static class Shaped extends JEIRecipeCharset implements IShapedCraftingRecipeWrapper {
        public Shaped(RecipeCharset recipe) {
            super(recipe);
        }

        @Override
        public int getWidth() {
            return recipe.getWidth();
        }

        @Override
        public int getHeight() {
            return recipe.getHeight();
        }
    }

    public static JEIRecipeCharset create(RecipeCharset recipe) {
        return recipe.getType() == RecipeCharset.Type.SHAPELESS ? new Shapeless(recipe) : new Shaped(recipe);
    }

    protected final RecipeCharset recipe;

    private JEIRecipeCharset(RecipeCharset recipe) {
        this.recipe = recipe;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getRegistryName();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, JEIPluginCharset.STACKS.expandRecipeItemStackInputs(recipe.getIngredients()));
        ingredients.setOutputLists(ItemStack.class, Collections.singletonList(Lists.newArrayList(recipe.getAllRecipeOutputs())));
    }
}
