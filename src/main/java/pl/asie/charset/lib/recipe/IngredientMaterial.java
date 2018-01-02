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
import java.util.List;

public class IngredientMaterial extends IngredientCharset {
    public static class Factory implements IIngredientFactory {
        @Nonnull
        @Override
        public Ingredient parse(JsonContext jsonContext, JsonObject jsonObject) {
            String tag = jsonObject.has("nbtKey") ? JsonUtils.getString(jsonObject, "nbtKey") : null;
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
    private final String[] types;
    private final String nbtTag;
    private boolean matchStack;
    private Ingredient dependency;

    protected IngredientMaterial(String nbtTag, String... types) {
        super();
        this.types = types;
        this.nbtTag = nbtTag;
        this.chain = null;
        this.dependencies = null;
    }

    protected IngredientMaterial(String nbtTag, String chain, boolean dummy, String... types) {
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
                return ItemMaterialRegistry.INSTANCE.matches(stack, types);
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
            Collection<ItemMaterial> mats = ItemMaterialRegistry.INSTANCE.getMaterialsByTypes(types);
            ItemStack[][] stacks = new ItemStack[mats.size()][1];
            int idx = 0;
            for (ItemMaterial material : mats) {
                stacks[idx++][0] = material.getStack();
            }
            return stacks;
        }
    }

    public void setRequireMatches(boolean requireMatches) {
        this.matchStack = requireMatches;
    }
}
