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

package pl.asie.charset.lib.wires;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import pl.asie.charset.lib.recipe.IRecipeResultBuilder;
import pl.asie.charset.lib.recipe.ingredient.IngredientCharset;
import pl.asie.charset.lib.recipe.OutputSupplier;
import pl.asie.charset.lib.recipe.RecipeCharset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecipeWireConversion extends RecipeCharset {
    private static List<ItemStack> getMatchingStacks(int offset) {
        List<ItemStack> stacks = new ArrayList<>();
        for (WireProvider provider : WireManager.REGISTRY) {
            if (provider.hasItemWire() && provider.hasSidedWire() && provider.hasFreestandingWire()) {
                stacks.add(new ItemStack(provider.getItemWire(), 1, offset));
            }
        }
        return stacks;
    }

    public static class IngredientWires extends IngredientCharset {
        private final int offset;

        protected IngredientWires(boolean freestanding) {
            super();
            offset = freestanding ? 1 : 0;
        }

        @Override
        public boolean arePermutationsDistinct() {
            return true;
        }

        @Override
        protected ItemStack[][] createMatchingStacks() {
            List<ItemStack> stacks = RecipeWireConversion.getMatchingStacks(offset);
            ItemStack[][] stackArray = new ItemStack[stacks.size()][1];
            for (int i = 0; i < stacks.size(); i++) {
                stackArray[i][0] = stacks.get(i);
            }
            return stackArray;
        }

        @Override
        public boolean hasMatchingStacks() {
            return true;
        }

        @Override
        public boolean matches(ItemStack stack, IRecipeResultBuilder builder) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemWire && ((stack.getMetadata() & 1) == offset)) {
                WireProvider provider = ((ItemWire) stack.getItem()).getWireProvider();
                return provider.hasFreestandingWire() && provider.hasSidedWire();
            } else {
                return false;
            }
        }
    }

    private final int outputOffset;

    public RecipeWireConversion(boolean freestanding) {
        super("charset:wire_convert", true);
        super.input = NonNullList.create();
        super.input.add(IngredientCharset.wrap(new IngredientWires(freestanding)));
        super.output = OutputSupplier.createStackOutputSupplier(ItemStack.EMPTY);
        super.width = 1;
        super.height = 1;
        this.outputOffset = freestanding ? 0 : 1;
    }

    @Override
    public List<ItemStack> getAllRecipeOutputs() {
        return getMatchingStacks(outputOffset);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack newStack = stack.copy();
                newStack.setCount(1);
                newStack.setItemDamage(newStack.getItemDamage() ^ 1);
                return newStack;
            }
        }

        return ItemStack.EMPTY;
    }
}
