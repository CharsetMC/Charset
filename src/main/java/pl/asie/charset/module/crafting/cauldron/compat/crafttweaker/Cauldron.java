package pl.asie.charset.module.crafting.cauldron.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.module.crafting.cauldron.api.CauldronContents;
import pl.asie.charset.module.crafting.cauldron.api.ICauldronRecipe;
import pl.asie.charset.module.crafting.cauldron.CharsetCraftingCauldron;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Optional;

@ZenRegister
@ZenClass("mods.charset.Cauldron")
@ModOnly("charset")
public class Cauldron {
	@ZenMethod
	public static void addItemRecipe(IIngredient inputItem, ILiquidStack inputLiquid, IItemStack outputItem) {
		addItemRecipe(inputItem, inputLiquid, outputItem, true);
	}

	@ZenMethod
	public static void addItemRecipe(IIngredient inputItem, ILiquidStack inputLiquid, IItemStack outputItem, boolean consumeLiquid) {
		if (inputItem != null) {
			CraftTweakerAPI.apply(new AddRecipeAction((cauldron, contents) -> {
				if (!contents.hasHeldItem() || !inputItem.matches(CraftTweakerMC.getIItemStack(contents.getHeldItem()))) {
					return Optional.empty();
				}

				if (inputLiquid == null) {
					if (contents.hasFluidStack()) {
						return Optional.empty();
					}
				} else {
					if (!contents.hasFluidStack()) {
						return Optional.empty();
					}

					if (contents.getFluidStack().amount < inputLiquid.getAmount()) {
						return Optional.empty();
					}

					// if (!inputLiquid.matches(new MCLiquidStack(contents.getFluidStack()).withAmount(inputLiquid.getAmount()))) {
					//	return Optional.empty();
					// }

					/* Workaround for CraftTweaker#449 */
					boolean found = false;
					for (ILiquidStack stack : inputLiquid.getLiquids()) {
						FluidStack compared = CraftTweakerMC.getLiquidStack(stack);
						if (compared != null && compared.isFluidEqual(contents.getFluidStack())) {
							found = true;
						}
					}
					if (!found) {
						return Optional.empty();
					}
				}

				FluidStack resultFluid = contents.getFluidStack().copy();
				if (consumeLiquid) {
					resultFluid.amount -= inputLiquid.getAmount();
					if (resultFluid.amount <= 0) {
						resultFluid = null;
					}
				}

				return Optional.of(new CauldronContents(resultFluid, CraftTweakerMC.getItemStack(outputItem)));
			}));
		}
	}

	@ZenMethod
	public static void addItemFluidRecipe(IIngredient inputItem, ILiquidStack inputLiquid, IItemStack outputItem, ILiquidStack outputLiquid) {
		addItemFluidRecipe(inputItem, inputLiquid, outputItem, outputLiquid, false);
	}

	@ZenMethod
	public static void addItemFluidRecipe(IIngredient inputItem, ILiquidStack inputLiquid, IItemStack outputItem, ILiquidStack outputLiquid, boolean ignoreLiquidAmount) {
		if (inputItem != null) {
			CraftTweakerAPI.apply(new AddRecipeAction((cauldron, contents) -> {
				if (!contents.hasHeldItem() || !inputItem.matches(CraftTweakerMC.getIItemStack(contents.getHeldItem()))) {
					return Optional.empty();
				}

				if (inputLiquid == null) {
					if (contents.hasFluidStack()) {
						return Optional.empty();
					}
				} else {
					if (!contents.hasFluidStack()) {
						return Optional.empty();
					}

					if (!ignoreLiquidAmount && contents.getFluidStack().amount != inputLiquid.getAmount()) {
						return Optional.empty();
					}

					/* IIngredient ms = inputLiquid.amount(contents.getFluidStack().amount);
					if (!ms.matches(new MCLiquidStack(contents.getFluidStack()))) {
						return Optional.empty();
					} */

					/* Workaround for CraftTweaker#449 */
					boolean found = false;
					for (ILiquidStack stack : inputLiquid.getLiquids()) {
						FluidStack compared = CraftTweakerMC.getLiquidStack(stack);
						if (compared != null && compared.isFluidEqual(contents.getFluidStack())) {
							found = true;
						}
					}
					if (!found) {
						return Optional.empty();
					}
				}

				FluidStack newStack = CraftTweakerMC.getLiquidStack(outputLiquid);
				if (ignoreLiquidAmount && contents.hasFluidStack()) {
					newStack = newStack.copy();
					newStack.amount = contents.getFluidStack().amount;
				}

				return Optional.of(new CauldronContents(newStack, CraftTweakerMC.getItemStack(outputItem)));
			}));
		}
	}

	public static class AddRecipeAction implements IAction {
		private final ICauldronRecipe recipe;

		public AddRecipeAction(ICauldronRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			CharsetCraftingCauldron.add(recipe);
		}

		@Override
		public String describe() {
			return "Adding Cauldron recipe";
		}
	}
}
