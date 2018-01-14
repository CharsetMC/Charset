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

package pl.asie.charset.lib.modcompat.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.item.IDyeableItem;
import pl.asie.charset.lib.recipe.DyeableItemRecipeFactory;
import pl.asie.charset.lib.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Collections;

public class JEIRecipeDyeableItem implements IRecipeWrapper {
    public static JEIRecipeDyeableItem create(DyeableItemRecipeFactory.Recipe recipe) {
        return new JEIRecipeDyeableItem(recipe);
    }

    protected final DyeableItemRecipeFactory.Recipe recipe;

    private JEIRecipeDyeableItem(DyeableItemRecipeFactory.Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, JEIPluginCharset.STACKS.expandRecipeItemStackInputs(
                Lists.newArrayList(recipe.input, recipe.DYE)
        ));

        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : recipe.input.getMatchingStacks()) {
            IDyeableItem item = (IDyeableItem) stack.getItem();
            for (EnumDyeColor color : EnumDyeColor.values()) {
                ItemStack stackColored = stack.copy();
                item.setColor(stackColored, ColorUtils.toIntColor(color));
                stacks.add(stackColored);
            }
        }

        ingredients.setOutputLists(ItemStack.class, Collections.singletonList(stacks));
    }
}
