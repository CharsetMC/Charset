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

package pl.asie.charset.module.storage.barrels;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import pl.asie.charset.lib.recipe.IOutputSupplier;
import pl.asie.charset.lib.recipe.IngredientMatcher;
import pl.asie.charset.lib.recipe.RecipeCharset;

public class BarrelCartOutputSupplier implements IOutputSupplier {
    @Override
    public ItemStack getCraftingResult(RecipeCharset recipe, IngredientMatcher matcher, InventoryCrafting inv) {
        for (Ingredient i : matcher.getMatchedIngredients()) {
            ItemStack is = matcher.getStack(i);
            if (is.getItem() == CharsetStorageBarrels.barrelItem) {
                return CharsetStorageBarrels.barrelCartItem.makeBarrelCart(is);
            }
        }
        return new ItemStack(CharsetStorageBarrels.barrelCartItem);
    }

    @Override
    public ItemStack getDefaultOutput() {
        return new ItemStack(CharsetStorageBarrels.barrelCartItem);
    }
}
