/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

import net.minecraft.block.Block;
import net.minecraft.block.BlockSponge;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.module.crafting.cauldron.api.CauldronContents;
import pl.asie.charset.module.crafting.cauldron.api.ICauldronRecipe;
import pl.asie.charset.module.crafting.cauldron.CharsetCraftingCauldron;
import pl.asie.charset.module.crafting.cauldron.api.ICauldron;

import java.util.Optional;

public class RecipeWashDyedWater implements ICauldronRecipe {
	@Override
	public Optional<CauldronContents> apply(ICauldron cauldron, CauldronContents contents) {
		if (!contents.hasFluidStack()) {
			return Optional.empty();
		}

		if (contents.getFluidStack().getFluid() == CharsetCraftingCauldron.dyedWater) {
			ItemStack heldItem = contents.getHeldItem();
			if (heldItem.getItem() instanceof ItemBlock && Block.getBlockFromItem(heldItem.getItem()) instanceof BlockSponge) {
				return Optional.of(new CauldronContents(new FluidStack(FluidRegistry.WATER, contents.getFluidStack().amount), heldItem));
			}
		}

		return Optional.empty();
	}
}
