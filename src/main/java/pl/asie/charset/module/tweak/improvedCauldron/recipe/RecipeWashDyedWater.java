package pl.asie.charset.module.tweak.improvedCauldron.recipe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSponge;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.module.tweak.improvedCauldron.CharsetTweakImprovedCauldron;
import pl.asie.charset.module.tweak.improvedCauldron.api.CauldronContents;
import pl.asie.charset.module.tweak.improvedCauldron.api.ICauldronRecipe;

import java.util.Optional;

public class RecipeWashDyedWater implements ICauldronRecipe {
	@Override
	public Optional<CauldronContents> apply(World world, BlockPos pos, CauldronContents contents) {
		if (!contents.hasFluidStack()) {
			return Optional.empty();
		}

		if (contents.getFluidStack().getFluid() == CharsetTweakImprovedCauldron.dyedWater) {
			ItemStack heldItem = contents.getHeldItem();
			if (heldItem.getItem() instanceof ItemBlock && Block.getBlockFromItem(heldItem.getItem()) instanceof BlockSponge) {
				return Optional.of(new CauldronContents(new FluidStack(FluidRegistry.WATER, contents.getFluidStack().amount), heldItem));
			}
		}

		return Optional.empty();
	}
}
