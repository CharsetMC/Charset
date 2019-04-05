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

package pl.asie.charset.lib.recipe;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.recipe.ingredient.IngredientCharset;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class IngredientMaterial extends IngredientCharset {
    public static class Factory implements IIngredientFactory {
        @Nonnull
        @Override
        public Ingredient parse(JsonContext jsonContext, JsonObject jsonObject) {
            String tag = jsonObject.has("nbtKey") ? JsonUtils.getString(jsonObject, "nbtKey") : null;
            Collection<String[]> material;

            JsonElement oreElem = jsonObject.get("material");
            if (oreElem instanceof JsonArray) {
                ImmutableList.Builder<String[]> builder = new ImmutableList.Builder<>();
                List<String> singleton = new ArrayList<>();
                JsonArray array = oreElem.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement arrayElem = array.get(i);
                    if (arrayElem instanceof JsonArray) {
                        JsonArray array2 = (JsonArray) arrayElem;
                        ImmutableList.Builder<String> singleton2 = new ImmutableList.Builder<>();
                        for (int j = 0; j < array2.size(); j++) {
                            singleton2.add(array2.get(j).getAsString());
                        }
                        builder.add(singleton2.build().toArray(new String[0]));
                    } else {
                        singleton.add(array.get(i).getAsString());
                    }
                }

                if (!singleton.isEmpty()) {
                    builder.add(singleton.toArray(new String[0]));
                }
                material = builder.build();
            } else {
                material = Collections.singletonList(new String[]{ JsonUtils.getString(jsonObject, "material")});
            }

            IngredientMaterial result;
            if (jsonObject.has("chain")) {
                result = new IngredientMaterial(tag, JsonUtils.getString(jsonObject, "chain"), false, material);
            } else {
                result = new IngredientMaterial(tag, material);
            }

            if (jsonObject.has("matchStack")) {
                result.setRequireMatches(JsonUtils.getBoolean(jsonObject, "matchStack"));
            }

            return IngredientCharset.wrap(result);
        }
    }

    private final String[] chain;
    private final TCharSet dependencies;
    private final Collection<String[]> types;
    private final String nbtTag;
    private boolean matchStack;
    private Ingredient dependency;

    protected IngredientMaterial(String nbtTag, Collection<String[]> types) {
        super();
        this.types = types;
        this.nbtTag = nbtTag;
        this.chain = null;
        this.dependencies = null;
    }

    protected IngredientMaterial(String nbtTag, String chain, boolean dummy, Collection<String[]> types) {
        super();
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
    public void onAdded(IRecipeView view) {
        if (this.chain != null) {
            char depChar = this.chain[0].charAt(0);
            dependency = view.getIngredient(depChar);
        }
    }

    @Override
    public boolean arePermutationsDistinct() {
        return super.arePermutationsDistinct() || matchStack || chain != null || nbtTag != null;
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
    public boolean matches(ItemStack stack, IRecipeResultBuilder builder) {
        if (chain != null) {
            ItemStack stackIn = builder.getStack(dependency);
            if (!stackIn.isEmpty()) {
                ItemMaterial base = getChainedMaterial(ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(stackIn));
                return base != null && ItemMaterialRegistry.INSTANCE.matches(stack, base);
            }
            return false;
        } else {
            if (!stack.isEmpty()) {
                for (String[] typeSet : types) {
                    if (ItemMaterialRegistry.INSTANCE.matches(stack, typeSet)) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    @Override
    public ItemStack transform(ItemStack stack, ItemStack source, IRecipeResultBuilder builder) {
        if (nbtTag != null) {
            ItemUtils.getTagCompound(stack, true).setString(nbtTag, ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(source).getId());
        }
        return stack;
    }

    @Override
    protected ItemStack[][] createMatchingStacks() {
        if (chain != null) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack stack : dependency.getMatchingStacks()) {
                ItemMaterial materialOut = getChainedMaterial(ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(stack));
                if (materialOut != null) {
                    stacks.add(materialOut.getStack());
                }
            }

            ItemStack[][] stackArrays = new ItemStack[stacks.size()][1];
            for (int i = 0; i < stacks.size(); i++) {
                stackArrays[i][0] = stacks.get(i);
            }
            return stackArrays;
        } else {
            Collection<ItemMaterial> mats;
            if (types.size() == 1) {
                mats = ItemMaterialRegistry.INSTANCE.getMaterialsByTypes(types.iterator().next());
            } else if (types.size() <= 0) {
                mats = Collections.emptyList();
            } else {
                mats = new ArrayList<>();
                for (String[] t : types) {
                    mats.addAll(ItemMaterialRegistry.INSTANCE.getMaterialsByTypes(t));
                }
            }

            ItemStack[][] stacks = new ItemStack[mats.size()][1];
            int idx = 0;
            for (ItemMaterial material : mats) {
                stacks[idx++][0] = material.getStack();
            }
            return stacks;
        }
    }

    @Override
    public boolean hasMatchingStacks() {
        return true;
    }

    public void setRequireMatches(boolean requireMatches) {
        this.matchStack = requireMatches;
    }
}
