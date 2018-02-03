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

    public static InventoryCrafting getCraftingInventory(int width, int height) {
        return new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        }, width, height);
    }

    public static InventoryCrafting getCraftingInventory(int width, int height, ItemStack... stacks) {
        InventoryCrafting crafting = getCraftingInventory(width, height);
        for (int i = 0; i < Math.min(width * height, stacks.length); i++) {
            crafting.setInventorySlotContents(i, (stacks[i] == null || stacks[i].isEmpty()) ? ItemStack.EMPTY : stacks[i].copy());
        }
        return crafting;
    }

    public static ItemStack getCraftingResult(World world, int width, int height, ItemStack... stacks) {
        return getCraftingResult(world, getCraftingInventory(width, height, stacks));
    }

    public static ItemStack getCraftingResult(World world, InventoryCrafting crafting) {
        IRecipe recipe = findMatchingRecipe(crafting, world);
        if (recipe != null) {
            return recipe.getCraftingResult(crafting);
        }

        return ItemStack.EMPTY;
    }

    public static IRecipe findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn) {
        int width = craftMatrix.getWidth();
        int height = craftMatrix.getHeight();
        for (IRecipe irecipe : ForgeRegistries.RECIPES) {
            if (irecipe.canFit(width, height) && irecipe.matches(craftMatrix, worldIn)) {
                return irecipe;
            }
        }

        return null;
    }
}
