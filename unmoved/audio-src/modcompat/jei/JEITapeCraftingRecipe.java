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

package pl.asie.charset.audio.modcompat.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.audio.recipe.RecipeTape;
import pl.asie.charset.audio.tape.ItemTape;
import pl.asie.charset.lib.modcompat.jei.JEIPluginCharsetLib;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JEITapeCraftingRecipe extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
	public static class Handler implements IRecipeHandler<RecipeTape> {

		@Nonnull
		@Override
		public Class<RecipeTape> getRecipeClass() {
			return RecipeTape.class;
		}

		@Nonnull
		@Override
		public String getRecipeCategoryUid(@Nonnull RecipeTape recipe) {
			return VanillaRecipeCategoryUid.CRAFTING;
		}

		@Nonnull
		@Override
		public IRecipeWrapper getRecipeWrapper(@Nonnull RecipeTape recipe) {
			return new JEITapeCraftingRecipe();
		}

		@Override
		public boolean isRecipeValid(@Nonnull RecipeTape recipe) {
			return true;
		}
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		Object[] inputs = new Object[9];

		List<String> mats = new ArrayList<String>();
		for (ItemTape.Material m : ItemTape.Material.values()) {
			if (OreDictionary.doesOreNameExist(m.oreDict)) {
				mats.add(m.oreDict);
			}
		}

		inputs[0] = inputs[1] = inputs[2] = mats;
		inputs[3] = inputs[5] = new ItemStack(ModCharsetAudio.tapeReelItem, 1, OreDictionary.WILDCARD_VALUE);
		inputs[6] = inputs[7] = inputs[8] = new ItemStack(Blocks.STONE_SLAB);

		ItemStack output = new ItemStack(ModCharsetAudio.tapeItem, 1, OreDictionary.WILDCARD_VALUE);

		ingredients.setInputLists(ItemStack.class, JEIPluginCharsetLib.STACKS.expandRecipeItemStackInputs(Arrays.asList(inputs)));
		ingredients.setOutputs(ItemStack.class, JEIPluginCharsetLib.STACKS.getSubtypes(output));
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
