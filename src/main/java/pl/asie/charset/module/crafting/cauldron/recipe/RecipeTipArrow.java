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
