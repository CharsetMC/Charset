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

import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import pl.asie.charset.lib.recipe.IRecipeObject;
import pl.asie.charset.lib.recipe.IRecipeResult;
import pl.asie.charset.lib.recipe.RecipeCharset;

import javax.annotation.Nullable;

public class BarrelUpgradeRecipes {
    private static final IRecipeObject hopper = IRecipeObject.of(Blocks.HOPPER);
    private static final IRecipeObject slime_ball = IRecipeObject.of("slimeball");
    private static final IRecipeObject web = IRecipeObject.of(Blocks.WEB);
    private static final IRecipeObject barrel = new IRecipeObject() {
        @Override
        public Object preview() {
            return BarrelRegistry.INSTANCE.getBarrels(TileEntityDayBarrel.Type.NORMAL);
        }

        @Override
        public boolean test(ItemStack stack) {
            if (stack.getItem() == CharsetStorageBarrels.barrelItem) {
                TileEntityDayBarrel rep = new TileEntityDayBarrel();
                rep.loadFromStack(stack);
                return rep.type == TileEntityDayBarrel.Type.NORMAL;
            } else {
                return false;
            }
        }
    };

    public static void addUpgradeRecipes() {
        RecipeSorter.register("factorization:barrel_upgrade", BarrelUpgrade.class, RecipeSorter.Category.SHAPED, "");

        if (CharsetStorageBarrels.isEnabled(TileEntityDayBarrel.Type.SILKY)) {
            GameRegistry.addRecipe(new BarrelUpgrade(TileEntityDayBarrel.Type.SILKY, 3, 3, new IRecipeObject[]{
                    web, web, web,
                    web, barrel, web,
                    web, web, web
            }));
        }

        if (CharsetStorageBarrels.isEnabled(TileEntityDayBarrel.Type.HOPPING)) {
            GameRegistry.addRecipe(new BarrelUpgrade(TileEntityDayBarrel.Type.HOPPING, 1, 3, new IRecipeObject[]{
                    hopper,
                    barrel,
                    hopper
            }));
        }

        if (CharsetStorageBarrels.isEnabled(TileEntityDayBarrel.Type.STICKY)) {
            GameRegistry.addRecipe(new BarrelUpgrade(TileEntityDayBarrel.Type.STICKY, 1, 3, new IRecipeObject[]{
                    slime_ball,
                    barrel,
                    slime_ball
            }));
        }
    }

    public static class BarrelUpgrade extends RecipeCharset implements IRecipeResult {
        final TileEntityDayBarrel.Type upgradeType;

        public BarrelUpgrade(TileEntityDayBarrel.Type upgrade, int width, int height, IRecipeObject[] inputs) {
            super.width = width;
            super.height = height;
            super.input = inputs;
            super.output = this;
            this.upgradeType = upgrade;
        }

        ItemStack grabBarrel(InventoryCrafting container) {
            for (int i = 0; i < container.getSizeInventory(); i++) {
                ItemStack is = container.getStackInSlot(i);
                if (is.getItem() != CharsetStorageBarrels.barrelItem) {
                    continue;
                }
                return is;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public Object preview() {
            return BarrelRegistry.INSTANCE.getBarrels(upgradeType);
        }

        @Nullable
        @Override
        public ItemStack apply(@Nullable InventoryCrafting input) {
            ItemStack is = grabBarrel(input);
            if (is.isEmpty()) return ItemStack.EMPTY; // Shouldn't happen?
            TileEntityDayBarrel rep = new TileEntityDayBarrel();
            rep.loadFromStack(is);
            rep.type = upgradeType;
            return rep.getPickedBlock();
        }
    }

}
