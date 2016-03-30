package pl.asie.charset.lib.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import pl.asie.charset.lib.utils.ColorUtils;

public class RecipeDyeableItem extends RecipeBase {
	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemStack target = null;
		List<ItemStack> dyes = new ArrayList<ItemStack>();

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack source = inv.getStackInSlot(i);
			if (source != null) {
				if (source.getItem() instanceof IDyeableItem) {
					if (target != null) {
						return false;
					} else {
						target = source;
					}
				} else if (ColorUtils.getColorIDFromDye(source) >= 0) {
					dyes.add(source);
				} else {
					return false;
				}
			}
		}

		return target != null && !dyes.isEmpty();
	}

	/**
	 * Returns an Item that is the result of this recipe
	 */
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack target = null;
		IDyeableItem targetItem = null;
		int[] color = new int[3];
		int scale = 0;
		int count = 0;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack source = inv.getStackInSlot(i);
			if (source != null) {
				float[] col = null;
				if (source.getItem() instanceof IDyeableItem) {
					targetItem = (IDyeableItem) source.getItem();
					target = source.copy();
					target.stackSize = 1;

					if (targetItem.hasColor(source)) {
						int c = targetItem.getColor(source);

						col = new float[]{
								(c >> 16 & 255) / 255.0F,
								(c >> 8 & 255) / 255.0F,
								(c & 255) / 255.0F
						};
					}
				} else {
					int dyeId = ColorUtils.getColorIDFromDye(source);
					if (dyeId >= 0) {
						col = EntitySheep.getDyeRgb(EnumDyeColor.byMetadata(dyeId));
					}
				}

				if (col != null) {
					scale = (int) (scale + Math.max(col[0], Math.max(col[1], col[2])) * 255.0F);
					color[0] += (int) (color[0] + col[0] * 255.0F);
					color[1] += (int) (color[1] + col[1] * 255.0F);
					color[2] += (int) (color[2] + col[2] * 255.0F);
					count++;
				}
			}
		}

		if (targetItem != null) {
			int i1 = color[0] / count;
			int j1 = color[1] / count;
			int k1 = color[2] / count;
			float f3 = (float) scale / (float) count;
			float f4 = (float) Math.max(i1, Math.max(j1, k1));
			i1 = (int) (i1 * f3 / f4);
			j1 = (int) (j1 * f3 / f4);
			k1 = (int) (k1 * f3 / f4);
			targetItem.setColor(target, (i1 << 16) + (j1 << 8) + k1);
			return target;
		}

		return null;
	}

	public ItemStack getRecipeOutput() {
		return null;
	}
}
