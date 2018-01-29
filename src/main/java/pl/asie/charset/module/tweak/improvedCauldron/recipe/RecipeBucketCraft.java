package pl.asie.charset.module.tweak.improvedCauldron.recipe;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.*;
import pl.asie.charset.lib.utils.FluidUtils;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.module.tweak.improvedCauldron.CharsetTweakImprovedCauldron;
import pl.asie.charset.module.tweak.improvedCauldron.api.CauldronContents;
import pl.asie.charset.module.tweak.improvedCauldron.api.ICauldronRecipe;

import java.util.Optional;

public class RecipeBucketCraft implements ICauldronRecipe {
	@Override
	public Optional<CauldronContents> apply(World world, BlockPos pos, CauldronContents contents) {
		if (!contents.hasFluidStack() || !contents.hasHeldItem()) {
			return Optional.empty();
		}

		FluidStack stack = contents.getFluidStack();
		ItemStack heldItem = contents.getHeldItem();

		if (stack.amount >= Fluid.BUCKET_VOLUME) {
			ItemStack filledBucket = FluidUtil.getFilledBucket(stack);
			InventoryCrafting inventoryCrafting = RecipeUtils.getCraftingInventory(2, 1, heldItem, filledBucket);
			IRecipe recipe = RecipeUtils.findMatchingRecipe(inventoryCrafting, world);

			if (recipe != null) {
				ItemStack result = recipe.getCraftingResult(inventoryCrafting);

				if (!result.isEmpty() && !ItemUtils.canMerge(heldItem, result)) {
					NonNullList<ItemStack> stacks = recipe.getRemainingItems(inventoryCrafting);
					if (stacks.size() >= 2 && ItemUtils.canMerge(stacks.get(1), ForgeHooks.getContainerItem(filledBucket))) {
						return Optional.of(new CauldronContents(
								new FluidStack(stack, stack.amount - Fluid.BUCKET_VOLUME),
								result
						));
					}
				}
			}
		}

		return Optional.empty();
	}
}
