package pl.asie.charset.audio.tape;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.oredict.OreDictionary;

import pl.asie.charset.audio.ModCharsetAudio;
import pl.asie.charset.lib.recipe.RecipeBase;

/**
 * Created by asie on 1/23/16.
 */
public class RecipeTape extends RecipeBase {
	private static final String TAPE_PATTERN = "mmmr rsss";

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemTape.Material material = null;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			boolean found = false;
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
					for (ItemTape.Material m : ItemTape.Material.values()) {
						if (material != null & material != m) {
							continue;
						}
						int oreId = OreDictionary.getOreID(m.oreDict);
						int[] oreIds = OreDictionary.getOreIDs(source);
						for (int j : oreIds) {
							if (oreId == j) {
								found = true;
								break;
							}
						}
						if (found) {
							material = m;
							break;
						}
					}
					if (!found) {
						return false;
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
			boolean found = false;
			ItemStack source = inv.getStackInSlot(i);

			switch (TAPE_PATTERN.charAt(i)) {
				case ' ':
					if (source != null) {
						return null;
					}
					break;
				case 'm':
					if (source == null) {
						return null;
					}
					for (ItemTape.Material m : ItemTape.Material.values()) {
						if (material != null & material != m) {
							continue;
						}
						int oreId = OreDictionary.getOreID(m.oreDict);
						int[] oreIds = OreDictionary.getOreIDs(source);
						for (int j : oreIds) {
							if (oreId == j) {
								found = true;
								break;
							}
						}
						if (found) {
							material = m;
							break;
						}
					}
					if (!found) {
						return null;
					}
					break;
				case 'r':
					if (source == null || source.getItem() != ModCharsetAudio.tapeReelItem) {
						return null;
					} else {
						totalTapeItems += source.getItemDamage();
					}
					break;
				case 's':
					if (source == null || source.getItem() != Item.getItemFromBlock(Blocks.STONE_SLAB)) {
						return null;
					}
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
