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
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.IForgeRegistry;
import pl.asie.charset.lib.recipe.IngredientCharset;
import pl.asie.charset.lib.recipe.IngredientMatcher;
import pl.asie.charset.lib.recipe.RecipeCharset;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

// TODO: Turn into JSONs
public class BarrelUpgradeRecipes {
    private static final Ingredient hopper = CraftingHelper.getIngredient(Blocks.HOPPER);
    private static final Ingredient slime_ball = CraftingHelper.getIngredient("slimeball");
    private static final Ingredient web = CraftingHelper.getIngredient(Blocks.WEB);
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

    public static void addUpgradeRecipes(IForgeRegistry<IRecipe> registry) {
        if (CharsetStorageBarrels.isEnabled(TileEntityDayBarrel.Upgrade.SILKY)) {
            registry.register(new BarrelUpgrade("barrel_upgrade", TileEntityDayBarrel.Upgrade.SILKY, 3, 3, new Ingredient[]{
                    web, web, web,
                    web, barrel, web,
                    web, web, web
            }).setRegistryName("charset:barrel_upgrade_silky"));
        }

        if (CharsetStorageBarrels.isEnabled(TileEntityDayBarrel.Upgrade.HOPPING)) {
            registry.register(new BarrelUpgrade("barrel_upgrade", TileEntityDayBarrel.Upgrade.HOPPING, 1, 3, new Ingredient[]{
                    hopper,
                    barrel,
                    hopper
            }).setRegistryName("charset:barrel_upgrade_hopping"));
        }

        if (CharsetStorageBarrels.isEnabled(TileEntityDayBarrel.Upgrade.STICKY)) {
            registry.register(new BarrelUpgrade("barrel_upgrade", TileEntityDayBarrel.Upgrade.STICKY, 1, 3, new Ingredient[]{
                    slime_ball,
                    barrel,
                    slime_ball
            }).setRegistryName("charset:barrel_upgrade_sticky"));
        }
    }

    public static class BarrelUpgrade extends RecipeCharset {
        final TileEntityDayBarrel.Upgrade upgradeType;

        public BarrelUpgrade(String group, TileEntityDayBarrel.Upgrade upgrade, int width, int height, Ingredient[] inputs) {
            super(group);
            super.width = width;
            super.height = height;
            super.input = NonNullList.create();
            for (int i = 0; i < inputs.length; i++)
                super.input.add(inputs[i]);
            super.output = new ItemStack(CharsetStorageBarrels.barrelItem);
            this.upgradeType = upgrade;
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

}
