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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class IngredientMaterialFactory implements IIngredientFactory {
    public static class IngredientMaterial extends IngredientCharset {
        private final String[] chain;
        private final TCharSet dependencies;
        private final String[] types;
        private final String nbtTag;
        private net.minecraft.item.crafting.Ingredient dependency;

        protected IngredientMaterial(String nbtTag, String... types) {
            super(0);
            this.types = types;
            this.nbtTag = nbtTag;
            this.chain = null;
            this.dependencies = null;
        }

        protected IngredientMaterial(String nbtTag, String chain, boolean dummy, String... types) {
            super(0);
            this.types = types;
            this.nbtTag = nbtTag;
            this.chain = chain.split("\\.");
            this.dependencies = new TCharHashSet();
            dependencies.add(this.chain[0].charAt(0));
        }

        @Override
        public TCharSet getDependencies() {
            return dependencies;
        }

        @Override
        public void addDependency(char c, net.minecraft.item.crafting.Ingredient i) {
            if (chain != null && c == chain[0].charAt(0)) {
                dependency = i;
            }
        }

        @Override
        public boolean mustIteratePermutations() {
            return super.mustIteratePermutations() || chain != null || nbtTag != null;
        }

        private ItemMaterial getChainedMaterial(ItemMaterial base) {
            for (int i = 1; i < chain.length; i++) {
                if (chain[i].charAt(0) == '?') {
                    ItemMaterial nextBase = base.getRelated(chain[i].substring(1));
                    if (nextBase != null)
                        base = nextBase;
                } else {
                    base = base.getRelated(chain[i]);
                    if (base == null)
                        return null;
                }
            }

            return base;
        }

        @Override
        public boolean apply(IngredientMatcher matcher, ItemStack stack) {
            if (chain != null) {
                ItemStack stackIn = matcher.getStack(dependency);
                if (!stackIn.isEmpty()) {
                    ItemMaterial base = getChainedMaterial(ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(stackIn));
                    return base != null && ItemMaterialRegistry.INSTANCE.matches(stack, base);
                } else {
                    return false;
                }
            } else {
                return apply(stack);
            }
        }

        @Override
        public void applyToStack(ItemStack stack, ItemStack source) {
            if (nbtTag != null) {
                ItemUtils.getTagCompound(stack, true).setString(nbtTag, ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(source).getId());
            }
        }

        @Override
        public ItemStack[] getMatchingStacks() {
            if (chain != null) {
                Collection<ItemStack> stacks = new ArrayList<>();
                for (ItemStack stack : dependency.getMatchingStacks()) {
                    ItemMaterial materialOut = getChainedMaterial(ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(stack));
                    if (materialOut != null) {
                        stacks.add(materialOut.getStack());
                    }
                }

                return stacks.toArray(new ItemStack[stacks.size()]);
            } else {
                Collection<ItemMaterial> mats = ItemMaterialRegistry.INSTANCE.getMaterialsByTypes(types);
                ItemStack[] stacks = new ItemStack[mats.size()];
                int idx = 0;
                for (ItemMaterial material : mats) {
                    stacks[idx++] = material.getStack();
                }
                return stacks;
            }
        }

        @Override
        public boolean apply(@Nullable ItemStack stack) {
            if (stack == null || stack.isEmpty())
                return false;

            return ItemMaterialRegistry.INSTANCE.matches(stack, types);
        }
    }

    @Nonnull
    @Override
    public IngredientMaterial parse(JsonContext jsonContext, JsonObject jsonObject) {
        String tag = JsonUtils.getString(jsonObject, "nbtKey");
        String[] material;

        JsonElement oreElem = jsonObject.get("material");
        if (oreElem instanceof JsonArray) {
            JsonArray array = oreElem.getAsJsonArray();
            material = new String[array.size()];
            for (int i = 0; i < array.size(); i++)
                material[i] = array.get(i).getAsString();
        } else {
            material = new String[]{ JsonUtils.getString(jsonObject, "material") };
        }

        if (jsonObject.has("chain")) {
            return new IngredientMaterial(tag, JsonUtils.getString(jsonObject, "chain"), false, material);
        } else {
            return new IngredientMaterial(tag, material);
        }
    }
}
