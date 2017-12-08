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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import pl.asie.charset.lib.recipe.IRecipeResultBuilder;
import pl.asie.charset.lib.recipe.ingredient.IngredientCharset;

import javax.annotation.Nonnull;
import java.util.*;

public class IngredientBarrel extends IngredientCharset {
    private boolean includeCarts;
    private Set<TileEntityDayBarrel.Upgrade> upgradeBlacklist;

    private static Set<TileEntityDayBarrel.Upgrade> setFromJson(JsonContext context, JsonObject jsonObject, String memberName) {
        if (jsonObject.has(memberName)) {
            ImmutableSet.Builder<TileEntityDayBarrel.Upgrade> builder = new ImmutableSet.Builder<>();
            JsonArray array = JsonUtils.getJsonArray(jsonObject, memberName);
            for (JsonElement element : array) {
                builder.add(TileEntityDayBarrel.Upgrade.valueOf(element.getAsString()));
            }
            return builder.build();
        } else {
            return Collections.emptySet();
        }
    }

    public IngredientBarrel(JsonContext context, JsonObject json) {
        super();
        includeCarts = JsonUtils.getBoolean(json, "carts", false);
        upgradeBlacklist = setFromJson(context, json, "upgradeBlacklist");
    }

    @Override
    public boolean arePermutationsDistinct() {
        return true;
    }

    @Override
    public ItemStack[][] getMatchingStacks() {
        List<ItemStack> stacks = CharsetStorageBarrels.BARRELS_NORMAL;
        List<ItemStack> stacks2 = stacks;
        if (includeCarts) {
            stacks2 = Lists.newArrayList();
            for (ItemStack s : stacks) {
                stacks2.add(s);
                if (includeCarts) {
                    stacks2.add(CharsetStorageBarrels.barrelCartItem.makeBarrelCart(s));
                }
            }
        }

        ItemStack[][] stackArray = new ItemStack[stacks2.size()][1];
        for (int i = 0; i < stacks2.size(); i++) {
            stackArray[i][0] = stacks2.get(i);
        }
        return stackArray;
    }

    @Override
    public boolean matches(ItemStack stack, IRecipeResultBuilder builder) {
        if (!stack.isEmpty() && (stack.getItem() == CharsetStorageBarrels.barrelItem || (includeCarts && stack.getItem() == CharsetStorageBarrels.barrelCartItem))) {
            if (!upgradeBlacklist.isEmpty()) {
                Set<TileEntityDayBarrel.Upgrade> upgrades = EnumSet.noneOf(TileEntityDayBarrel.Upgrade.class);
                if (stack.hasTagCompound()) {
                    TileEntityDayBarrel.populateUpgrades(upgrades, stack.getTagCompound());
                }
                for (TileEntityDayBarrel.Upgrade upgrade : upgradeBlacklist) {
                    if (upgrades.contains(upgrade)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static class Factory implements IIngredientFactory {
        @Nonnull
        @Override
        public Ingredient parse(JsonContext context, JsonObject json) {
            return IngredientCharset.wrap(new IngredientBarrel(context, json));
        }
    }
}
