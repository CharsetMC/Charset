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

package pl.asie.charset.module.crafting.cauldron.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.*;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.module.crafting.cauldron.api.CauldronContents;
import pl.asie.charset.module.crafting.cauldron.api.ICauldronRecipe;
import pl.asie.charset.module.crafting.cauldron.api.ICauldron;

import java.util.Optional;

public class RecipeBucketCraft implements ICauldronRecipe {
	@Override
	public Optional<CauldronContents> apply(ICauldron cauldron, CauldronContents contents) {
		if (!contents.hasFluidStack() || !contents.hasHeldItem()) {
			return Optional.empty();
		}

		FluidStack stack = contents.getFluidStack();
		ItemStack heldItem = contents.getHeldItem();

		if (stack.amount >= Fluid.BUCKET_VOLUME) {
			ItemStack filledBucket = FluidUtil.getFilledBucket(stack);
			InventoryCrafting inventoryCrafting = RecipeUtils.getCraftingInventory(2, 1, heldItem, filledBucket);
			IRecipe recipe = RecipeUtils.findMatchingRecipe(inventoryCrafting, cauldron.getCauldronWorld());

			if (recipe != null) {
				ItemStack result = recipe.getCraftingResult(inventoryCrafting);

				if (!result.isEmpty() && !ItemUtils.canMerge(heldItem, result)) {
					NonNullList<ItemStack> stacks = recipe.getRemainingItems(inventoryCrafting);
					if (stacks.size() >= 2 && ItemUtils.canMerge(stacks.get(1), ForgeHooks.getContainerItem(filledBucket))) {
						return Optional.of(new CauldronContents(
								new FluidStack(stack, stack.amount - Fluid.BUCKET_VOLUME),
								result
						));
					}
				}
			}
		}

		return Optional.empty();
	}
}
