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

package pl.asie.charset.lib.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;

public final class RecipeUtils {
    private RecipeUtils() {

    }

    public static Container defaultContainer() {
        return new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        };
    }

    public static Container defaultContainer(EntityPlayer player) {
        return new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return playerIn == player;
            }
        };
    }

    public static InventoryCrafting getCraftingInventory(int width, int height) {
        return getCraftingInventory(width, height, defaultContainer());
    }

    public static InventoryCrafting getCraftingInventory(int width, int height, ItemStack... stacks) {
        return getCraftingInventory(width, height, defaultContainer(), stacks);
    }

    public static InventoryCrafting getCraftingInventory(int width, int height, Container container) {
        return new InventoryCrafting(container, width, height);
    }

    public static InventoryCrafting getCraftingInventory(int width, int height, Container container, ItemStack... stacks) {
        InventoryCrafting crafting = getCraftingInventory(width, height, container);
        for (int i = 0; i < Math.min(width * height, stacks.length); i++) {
            crafting.setInventorySlotContents(i, (stacks[i] == null || stacks[i].isEmpty()) ? ItemStack.EMPTY : stacks[i].copy());
        }
        return crafting;
    }
}
