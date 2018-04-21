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

package pl.asie.charset.lib.recipe;

import gnu.trove.iterator.TCharIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.ItemStackHashSet;
import pl.asie.charset.lib.utils.MethodHandleHelper;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.function.Function;

// TODO: Add support for smelting? Brewing?
public class RecipeIngredientPatcher {
    public static final RecipeIngredientPatcher PRIMARY = new RecipeIngredientPatcher();
    private final List<Function<Ingredient, Ingredient>> ingredientReplacer;

    public RecipeIngredientPatcher() {
        this.ingredientReplacer = new ArrayList<>();
    }

    public void add(Function<Ingredient, Ingredient> ing) {
        ingredientReplacer.add(ing);
    }

    public void process(Collection<IRecipe> registry) {
        for (IRecipe recipe : registry) {
            ResourceLocation recipeName = recipe.getRegistryName();
            boolean dirty = false;

            if (recipe instanceof ShapedRecipes || recipe instanceof ShapelessRecipes || recipe instanceof ShapedOreRecipe || recipe instanceof ShapelessOreRecipe) {
                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                for (int i = 0; i < ingredients.size(); i++) {
                    Ingredient ing = ingredients.get(i);
                    Ingredient ingNew = ing;
                    for (Function<Ingredient, Ingredient> ir : ingredientReplacer) {
                        ingNew = ir.apply(ingNew);
                    }
                    if (ingNew != null && ingNew != ing) {
                        ingredients.set(i, ingNew);
                        dirty = true;
                    }
                }
            } else if (recipe instanceof RecipeCharset) {
                TCharObjectMap<Ingredient> charToIngredient = ((RecipeCharset) recipe).charToIngredient;
                NonNullList<Ingredient> ingredients = recipe.getIngredients();
                for (int i = 0; i < ingredients.size(); i++) {
                    Ingredient ing = ingredients.get(i);
                    Ingredient ingNew = ing;
                    for (Function<Ingredient, Ingredient> ir : ingredientReplacer) {
                        ingNew = ir.apply(ingNew);
                    }
                    if (ingNew != null && ingNew != ing) {
                        ingredients.set(i, ingNew);
                        TCharIterator iterator = charToIngredient.keySet().iterator();
                        while (iterator.hasNext()) {
                            char c = iterator.next();
                            if (charToIngredient.get(c) == ing) {
                                charToIngredient.put(c, ing);
                            }
                        }
                        dirty = true;
                    }
                }
            }

            if (dirty) {
                ModCharset.logger.info("Successfully edited " + recipeName + "!");
            }
        }
    }
}
