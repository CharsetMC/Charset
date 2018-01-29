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

package pl.asie.charset.module.tweak.improvedCauldron.recipe;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.module.tweak.improvedCauldron.CharsetTweakImprovedCauldron;
import pl.asie.charset.module.tweak.improvedCauldron.TileCauldronCharset;
import pl.asie.charset.module.tweak.improvedCauldron.api.CauldronContents;
import pl.asie.charset.module.tweak.improvedCauldron.api.ICauldronRecipe;

import java.util.Optional;

public class RecipeDyeItem implements ICauldronRecipe {
	@Override
	public Optional<CauldronContents> apply(World world, BlockPos pos, CauldronContents contents) {
		if (!contents.hasFluidStack() || !contents.hasHeldItem()) {
			return Optional.empty();
		}

		FluidStack stack = contents.getFluidStack();
		ItemStack heldItem = contents.getHeldItem();

		if (stack.getFluid() == CharsetTweakImprovedCauldron.dyedWater
				&& stack.tag != null
				&& stack.amount >= 250
				&& stack.tag.hasKey("dyes", Constants.NBT.TAG_LIST)) {

			NBTTagList dyes = (NBTTagList) stack.tag.getTag("dyes");
			ItemStack[] stacks = new ItemStack[9];
			stacks[0] = heldItem.copy();
			stacks[0].setCount(1);
			for (int i = 0; i < 8; i++) {
				if (i < dyes.tagCount()) {
					stacks[i + 1] = new ItemStack(Items.DYE, 1, 15 - ((NBTPrimitive) dyes.get(i)).getByte());
				} else {
					stacks[i + 1] = ItemStack.EMPTY;
				}
			}

			ItemStack result = RecipeUtils.getCraftingResult(world, 3, 3, stacks);
			if (!result.isEmpty() && !ItemUtils.canMerge(stacks[0], result)) {
				return Optional.of(new CauldronContents(
						new FluidStack(stack, stack.amount - 250),
						result
				));
			}
		}

		return Optional.empty();
	}
}
