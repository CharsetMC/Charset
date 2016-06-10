/*
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

package pl.asie.charset.audio.integration.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.audio.tape.RecipeTapeReel;

public class JEITapeReelCraftingRecipe extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
	public static class Handler implements IRecipeHandler<RecipeTapeReel> {

		@Nonnull
		@Override
		public Class<RecipeTapeReel> getRecipeClass() {
			return RecipeTapeReel.class;
		}

		@Nonnull
		@Override
		public String getRecipeCategoryUid() {
			return VanillaRecipeCategoryUid.CRAFTING;
		}

		@Nonnull
		@Override
		public String getRecipeCategoryUid(@Nonnull RecipeTapeReel recipe) {
			return VanillaRecipeCategoryUid.CRAFTING;
		}

		@Nonnull
		@Override
		public IRecipeWrapper getRecipeWrapper(@Nonnull RecipeTapeReel recipe) {
			return new JEITapeReelCraftingRecipe();
		}

		@Override
		public boolean isRecipeValid(@Nonnull RecipeTapeReel recipe) {
			return true;
		}
	}

	@Nonnull
	@Override
	public List getInputs() {
		Object[] inputs = new Object[9];

		List<Object> mats = new ArrayList<Object>();
		mats.add(new ItemStack(ModCharsetAudio.magneticTapeItem));
		mats.add(null);

		for (int i = 0; i < 9; i++) {
			if (i == 4) {
				inputs[4] = new ItemStack(ModCharsetAudio.tapeReelItem, 1, OreDictionary.WILDCARD_VALUE);
			} else {
				inputs[i] = mats;
			}
		}

		return Arrays.asList(inputs);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(new ItemStack(ModCharsetAudio.tapeReelItem, 1, OreDictionary.WILDCARD_VALUE));
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 3;
	}

}
