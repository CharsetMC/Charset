package pl.asie.charset.module.tweak.improvedCauldron.patches;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.charset.lib.recipe.RecipeBase;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nullable;

public class RecipeHideItemDyeing extends RecipeBase {
	public RecipeHideItemDyeing(String group) {
		super(group, true);
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		int dyeCount = 0;
		int nonDyeItemCount = 0;
		ItemStack nonDyeItem = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				if (ColorUtils.getDyeColor(stack) != null) {
					dyeCount++;
				} else if (nonDyeItem.isEmpty()) {
					nonDyeItem = stack;
					nonDyeItemCount = 1;
				} else if (ItemUtils.canMerge(nonDyeItem, stack)) {
					nonDyeItemCount++;
				} else {
					return false;
				}
			}
		}

		return (nonDyeItemCount == 1 && (dyeCount >= 1 && dyeCount <= 8))
				|| (nonDyeItemCount == 8 && dyeCount == 1);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height) {
		return (width * height) >= 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}
}
