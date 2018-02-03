package pl.asie.charset.module.tweak.improvedCauldron.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;

public interface ICauldronRecipeViewable extends ICauldronRecipe {
	Collection<CauldronContents> getRecipesForInputItem(ItemStack stack);
	Collection<CauldronContents> getRecipesForInputFluid(FluidStack stack);
	Collection<CauldronContents> getRecipesForOutputItem(ItemStack stack);
	Collection<CauldronContents> getRecipesForOutputFluid(FluidStack stack);
}
