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

package pl.asie.charset.audio.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.recipe.RecipeBase;

public class RecipeTapeReel extends RecipeBase {
	private int getMetadata(InventoryCrafting inv) {
		int newMeta = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack source = inv.getStackInSlot(i);
			if (source == null) {
				continue;
			} else if (source.getItem() == ModCharsetAudio.magneticTapeItem) {
				newMeta++;
			} else if (source.getItem() == ModCharsetAudio.tapeReelItem) {
				newMeta += source.getItemDamage();
			} else {
				return -1;
			}
		}
		return newMeta;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		int newMeta = getMetadata(inv);
		return newMeta > 0 && newMeta <= 128;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		int newMeta = getMetadata(inv);
		if (newMeta > 0 && newMeta <= 128) {
			return new ItemStack(ModCharsetAudio.tapeReelItem, 1, newMeta);
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}
}
