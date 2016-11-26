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

package pl.asie.charset.storage.barrel;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import pl.asie.charset.storage.ModCharsetStorage;

public class BarrelUpgradeRecipes {
    private static final ItemStack oakLog = new ItemStack(Blocks.LOG);
    private static final ItemStack oakPlank = new ItemStack(Blocks.WOODEN_SLAB);
    private static final ItemStack hopper = new ItemStack(Blocks.HOPPER);
    private static final ItemStack slime_ball = new ItemStack(Items.SLIME_BALL);
    private static final ItemStack web = new ItemStack(Blocks.WEB);

    public static void addUpgradeRecipes() {
        ItemStack oakBarrel = TileEntityDayBarrel.makeBarrel(TileEntityDayBarrel.Type.NORMAL, oakLog, oakPlank);
        oakBarrel.setItemDamage(OreDictionary.WILDCARD_VALUE);

        RecipeSorter.register("factorization:barrel_upgrade", BarrelUpgrade.class, RecipeSorter.Category.SHAPED, "");

        GameRegistry.addRecipe(new BarrelUpgrade(TileEntityDayBarrel.Type.SILKY, 3, 3, new ItemStack[] {
                web, web, web,
                web, oakBarrel, web,
                web, web, web
        }));

        GameRegistry.addRecipe(new BarrelUpgrade(TileEntityDayBarrel.Type.HOPPING, 1, 3, new ItemStack[] {
                hopper,
                oakBarrel,
                hopper
        }));

        GameRegistry.addRecipe(new BarrelUpgrade(TileEntityDayBarrel.Type.STICKY, 1, 3, new ItemStack[] {
                slime_ball,
                oakBarrel,
                slime_ball
        }));
    }

    public static class BarrelUpgrade extends ShapedRecipes {
        final TileEntityDayBarrel.Type upgradeType;

        public BarrelUpgrade(TileEntityDayBarrel.Type upgrade, int width, int height, ItemStack[] inputs) {
            super(width, height, inputs, TileEntityDayBarrel.makeBarrel(upgrade, oakLog, oakPlank));
            this.upgradeType = upgrade;
        }

        ItemStack grabBarrel(InventoryCrafting container) {
            for (int i = 0; i < container.getSizeInventory(); i++) {
                ItemStack is = container.getStackInSlot(i);
                if (is.getItem() != ModCharsetStorage.barrelItem) {
                    continue;
                }
                return is;
            }
            return null;
        }

        @Override
        public boolean matches(InventoryCrafting container, World world) {
            if (!super.matches(container, world)) return false;
            ItemStack is = grabBarrel(container);
            if (is == null) return false;
            TileEntityDayBarrel rep = new TileEntityDayBarrel();
            rep.loadFromStack(is);
            return rep.type == TileEntityDayBarrel.Type.NORMAL;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting container) {
            ItemStack is = grabBarrel(container);
            if (is == null) return super.getCraftingResult(container); // Shouldn't happen?
            TileEntityDayBarrel rep = new TileEntityDayBarrel();
            rep.loadFromStack(is);
            rep.type = upgradeType;
            return rep.getPickedBlock();
        }
    }

}
