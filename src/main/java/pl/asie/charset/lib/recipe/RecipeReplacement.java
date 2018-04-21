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
import gnu.trove.map.hash.TIntIntHashMap;
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
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class RecipeReplacement {
    public static final RecipeReplacement PRIMARY = new RecipeReplacement();
    private static final MethodHandle ORES_GETTER = MethodHandleHelper.findFieldGetter(OreIngredient.class, "ores");

    private final Map<ItemStack, Object> replacements = new TCustomHashMap<>(
            new ItemStackHashSet.Strategy(false, true, true)
    );
    private final Map<Item, Object> replaceableItems = new IdentityHashMap<>();
    private final TIntObjectMap<Object> replaceableOres = new TIntObjectHashMap<>();

    public RecipeReplacement() {

    }

    public void register() {
        RecipeIngredientPatcher.PRIMARY.add(this::replaceIngredient);
    }

    private void checkTo(Object to) {
        if (!(to instanceof Item || to instanceof ItemStack || to instanceof String)) {
            throw new RuntimeException("Invalid RecipeReplacement target type: " + to.getClass().getName() + "!");
        }
    }

    public void add(ItemStack from, Object to) {
        checkTo(to);
        if (from.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            replaceableItems.put(from.getItem(), to);
        } else {
            replacements.put(from, to);
        }
    }

    public void add(Item from, Object to) {
        checkTo(to);
        replaceableItems.put(from, to);
    }

    public void add(String from, Object to) {
        checkTo(to);
        replaceableOres.put(OreDictionary.getOreID(from), to);
    }

    @Nullable
    private Ingredient replaceIngredient(Ingredient ing) {
        if (ing.getClass() == Ingredient.class || ing.getClass() == IngredientNBT.class) {
            boolean checkNBT = ing.getClass() == IngredientNBT.class;
            ItemStack[] matchingStacks = ing.getMatchingStacks();
            ItemStack[] matchingStacksNew = null;
            int replacementOreMatches = 0;
            String replacementOre = null;
            boolean dirty = false;

            for (int j = 0; j < matchingStacks.length; j++) {
                ItemStack stack = matchingStacks[j];
                if (stack.isEmpty()) continue;

                ItemStack newStack = null;
                Object replacement = null;

                for (int i : OreDictionary.getOreIDs(stack)) {
                    if (replaceableOres.containsKey(i)) {
                        replacement = replaceableOres.get(i);
                        break;
                    }
                }

                if (replacement == null) {
                    if (replacements.containsKey(stack)) {
                        replacement = replacements.get(stack);
                    } else if (!checkNBT && replaceableItems.containsKey(stack.getItem())) {
                        replacement = replaceableItems.get(stack.getItem());
                    }
                }

                if (replacement instanceof Item) {
                    newStack = new ItemStack((Item) replacement, stack.getCount(), stack.getItemDamage());
                    newStack.setTagCompound(stack.getTagCompound());
                } else if (replacement instanceof ItemStack) {
                    newStack = ((ItemStack) replacements.get(stack)).copy();
                    newStack.setCount(stack.getCount());
                } else if (replacement instanceof String) {
                    replacementOreMatches++;
                    replacementOre = (String) replacement;
                }

                if (newStack != null) {
                    if (!dirty) {
                        matchingStacksNew = new ItemStack[matchingStacks.length];
                        System.arraycopy(matchingStacks, 0, matchingStacksNew, 0, matchingStacks.length);
                        dirty = true;
                    }

                    matchingStacksNew[j] = newStack;
                }
            }

            if (replacementOre != null && replacementOreMatches == matchingStacks.length)  {
                return new OreIngredient(replacementOre);
            } else {
                if (matchingStacksNew != null) {
                    return Ingredient.fromStacks(matchingStacksNew);
                }
            }
        } else if (ing.getClass() == OreIngredient.class) {
            try {
                NonNullList<ItemStack> list = (NonNullList<ItemStack>) ORES_GETTER.invokeExact((OreIngredient) ing);
                if (list.isEmpty()) {
                    return null;
                }

                TIntIterator it = replaceableOres.keySet().iterator();
                while (it.hasNext()) {
                    int oreId = it.next();
                    NonNullList<ItemStack> oreList = OreDictionary.getOres(OreDictionary.getOreName(oreId));
                    if (!oreList.isEmpty() && list == oreList) {
                        Object replacement = replaceableOres.get(oreId);
                        if (replacement instanceof Item) {
                            return Ingredient.fromItem((Item) replacement);
                        } else if (replacement instanceof ItemStack) {
                            return Ingredient.fromStacks((ItemStack) replacement);
                        } else if (replacement instanceof String) {
                            return new OreIngredient((String) replacement);
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return null;
    }
}
