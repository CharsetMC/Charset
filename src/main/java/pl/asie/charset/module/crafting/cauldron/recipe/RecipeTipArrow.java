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

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.module.crafting.cauldron.CharsetCraftingCauldron;
import pl.asie.charset.module.crafting.cauldron.api.CauldronContents;
import pl.asie.charset.module.crafting.cauldron.api.ICauldron;
import pl.asie.charset.module.crafting.cauldron.api.ICauldronRecipe;
import pl.asie.charset.module.crafting.cauldron.fluid.FluidPotion;

import java.util.Optional;

// m'potion
public class RecipeTipArrow implements ICauldronRecipe {
	@Override
	public Optional<CauldronContents> apply(ICauldron cauldron, CauldronContents contents) {
		if (!contents.hasFluidStack() || contents.getFluidStack().getFluid() != CharsetCraftingCauldron.liquidLingeringPotion) {
			return Optional.empty();
		}

		if (contents.getHeldItem().isEmpty() || contents.getHeldItem().getItem() != Items.ARROW) {
			return Optional.empty();
		}

		int amount = 0;
		for (int i = CharsetCraftingCauldron.maxArrowTipMultiplier; i >= 1; i--) {
			if (CharsetCraftingCauldron.waterBottleSize % i == 0) {
				amount = CharsetCraftingCauldron.waterBottleSize / i;
				break;
			}
		}

		amount *= contents.getHeldItem().getCount();

		if (contents.getFluidStack().amount < amount) {
			return Optional.empty();
		}

		ItemStack tippedArrow = new ItemStack(Items.TIPPED_ARROW, contents.getHeldItem().getCount(), 0);
		FluidPotion.copyToPotionItem(tippedArrow, contents.getFluidStack());

		return Optional.of(new CauldronContents(
				new FluidStack(contents.getFluidStack(), contents.getFluidStack().amount - amount),
				tippedArrow
		));
	}
}
