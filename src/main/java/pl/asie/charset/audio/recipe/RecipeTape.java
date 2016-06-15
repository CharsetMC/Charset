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

import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.oredict.OreDictionary;

import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.audio.tape.ItemTape;
import pl.asie.charset.lib.recipe.RecipeBase;

public class RecipeTape extends RecipeBase {
	private static final String TAPE_PATTERN = "mmmr rsss";

	private ItemTape.Material getMaterial(ItemStack source, ItemTape.Material previous) {
		int[] oreIds = OreDictionary.getOreIDs(source);

		if (previous != null) {
			int oreId = OreDictionary.getOreID(previous.oreDict);
			for (int j : oreIds) {
				if (oreId == j) {
					return previous;
				}
			}
		} else {
			for (ItemTape.Material m : ItemTape.Material.values()) {
				int oreId = OreDictionary.getOreID(m.oreDict);
				for (int j : oreIds) {
					if (oreId == j) {
						return m;
					}
				}
			}
		}

		return null;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemTape.Material material = null;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack source = inv.getStackInSlot(i);

			switch (TAPE_PATTERN.charAt(i)) {
				case ' ':
					if (source != null) {
						return false;
					}
					break;
				case 'm':
					if (source == null) {
						return false;
					}
					ItemTape.Material newMaterial = getMaterial(source, material);
					if (newMaterial == null) {
						return false;
					} else {
						material = newMaterial;
					}
					break;
				case 'r':
					if (source == null || source.getItem() != ModCharsetAudio.tapeReelItem) {
						return false;
					}
					break;
				case 's':
					if (source == null || source.getItem() != Item.getItemFromBlock(Blocks.STONE_SLAB)) {
						return false;
					}
					break;
			}
		}

		return true;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemTape.Material material = null;
		int totalTapeItems = 0;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack source = inv.getStackInSlot(i);

			switch (TAPE_PATTERN.charAt(i)) {
				case 'm':
					ItemTape.Material newMaterial = getMaterial(source, material);
					if (newMaterial == null) {
						return null;
					} else {
						material = newMaterial;
					}
					break;
				case 'r':
					totalTapeItems += source.getItemDamage();
					break;
			}
		}

		if (material != null && totalTapeItems > 0) {
			int size = totalTapeItems * 15 * ItemTape.DEFAULT_SAMPLE_RATE / 8;
			return ItemTape.asItemStack(size, material);
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}
}
