package pl.asie.charset.module.tweak.improvedCauldron.recipe;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.module.tweak.improvedCauldron.CharsetTweakImprovedCauldron;
import pl.asie.charset.module.tweak.improvedCauldron.TileCauldronCharset;
import pl.asie.charset.module.tweak.improvedCauldron.api.CauldronContents;
import pl.asie.charset.module.tweak.improvedCauldron.api.ICauldronRecipe;

import java.util.Optional;

public class RecipeDyeWater implements ICauldronRecipe {
	@Override
	public Optional<CauldronContents> apply(World world, BlockPos pos, CauldronContents contents) {
		if (!contents.hasFluidStack()) {
			return Optional.empty();
		}

		EnumDyeColor color = ColorUtils.getDyeColor(contents.getHeldItem());
		if (color != null) {
			FluidStack stack = contents.getFluidStack();
			if (stack.getFluid() == FluidRegistry.WATER || stack.getFluid() == CharsetTweakImprovedCauldron.dyedWater) {
				FluidStack newStack = CharsetTweakImprovedCauldron.dyedWater.appendDye(stack, color);
				if (newStack == null) {
					return Optional.of(new CauldronContents(new TextComponentTranslation("notice.charset.cauldron.no_dye")));
				} else {
					return Optional.of(new CauldronContents(newStack, ItemStack.EMPTY));
				}
			}
		}

		return Optional.empty();
	}
}
