/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.module.storage.barrels;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.registries.IForgeRegistry;
import pl.asie.charset.lib.recipe.IngredientCharset;
import pl.asie.charset.lib.recipe.IngredientMatcher;
import pl.asie.charset.lib.recipe.RecipeCharset;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;

public class BarrelUpgradeRecipe extends RecipeCharset {
    private static final Ingredient barrel = new IngredientCharset(0) {
        @Override
        public boolean mustIteratePermutations() {
            return true;
        }

        @Override
        public ItemStack[] getMatchingStacks() {
            Collection<ItemStack> stacks = CharsetStorageBarrels.BARRELS_NORMAL;
            Collection<ItemStack> stacks2 = Lists.newArrayList();
            for (ItemStack s : stacks) {
                stacks2.add(s);
                stacks2.add(CharsetStorageBarrels.barrelCartItem.makeBarrelCart(s));
            }
            return stacks.toArray(new ItemStack[stacks.size()]);
        }

        @Override
        public boolean apply(ItemStack stack) {
            if (!stack.isEmpty() && (stack.getItem() == CharsetStorageBarrels.barrelItem || stack.getItem() == CharsetStorageBarrels.barrelCartItem)) {
                return true;
            } else {
                return false;
            }
        }
    };


    public static class ConditionFactory implements IConditionFactory {
        @Override
        public BooleanSupplier parse(JsonContext context, JsonObject json) {
            String upgrade = JsonUtils.getString(json, "upgrade");
            TileEntityDayBarrel.Upgrade upgradeEnum = TileEntityDayBarrel.Upgrade.valueOf(upgrade);
            return () -> CharsetStorageBarrels.isEnabled(upgradeEnum);
        }
    }

    public static class Factory extends RecipeCharset.Factory {
        @Override
        protected String getType(JsonContext context, JsonObject json) {
            return JsonUtils.getString(json, "type");
        }

        @Override
        protected Ingredient parseIngredient(JsonElement json, JsonContext context) {
            Ingredient ingredient = CraftingHelper.getIngredient(json, context);
            if (ingredient.apply(new ItemStack(CharsetStorageBarrels.barrelItem))) {
                return barrel;
            } else {
                return ingredient;
            }
        }

        @Override
        public IRecipe parse(JsonContext context, JsonObject json) {
            BarrelUpgradeRecipe recipe = new BarrelUpgradeRecipe(context, json);
            String type = getType(context, json);

            if (type.endsWith("shapeless")) {
                parseInputShapeless(recipe, context, json);
            } else if (type.endsWith("shaped")) {
                parseInputShaped(recipe, context, json);
            } else {
                throw new RuntimeException("Unknown type: " + type);
            }

            recipe.upgradeType = TileEntityDayBarrel.Upgrade.valueOf(JsonUtils.getString(json, "upgrade"));
            recipe.output = new ItemStack(CharsetStorageBarrels.barrelItem);
            return recipe;
        }
    }

    TileEntityDayBarrel.Upgrade upgradeType;

    public BarrelUpgradeRecipe(JsonContext context, JsonObject json) {
        super(context, json);
    }

    ItemStack grabBarrel(InventoryCrafting container) {
        for (int i = 0; i < container.getSizeInventory(); i++) {
            ItemStack is = container.getStackInSlot(i);
            if (is.getItem() != CharsetStorageBarrels.barrelItem && is.getItem() != CharsetStorageBarrels.barrelCartItem) {
                continue;
            }
            return is;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Collection<ItemStack> getAllRecipeOutputs() {
        Collection<ItemStack> stacks = CharsetStorageBarrels.BARRELS_NORMAL;
        Collection<ItemStack> stacks2 = new ArrayList<>();
        for (ItemStack s : stacks) {
            stacks2.add(TileEntityDayBarrel.addUpgrade(s, upgradeType));
        }
        return stacks2;
    }

    @Nullable
    @Override
    public ItemStack getCraftingResult(@Nullable InventoryCrafting input) {
        IngredientMatcher matcher = super.matchedOrNull(input);
        if (matcher != null) {
            ItemStack is = grabBarrel(input);
            if (is.isEmpty()) return ItemStack.EMPTY; // Shouldn't happen?
            is = is.copy();
            is.setCount(1);
            return TileEntityDayBarrel.addUpgrade(is, upgradeType);
        } else {
            return ItemStack.EMPTY;
        }
    }
}
