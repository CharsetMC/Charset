package pl.asie.charset.module.storage.tanks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.utils.ColorUtils;

import javax.annotation.Nullable;

public final class TankTintHandler implements IBlockColor, IItemColor {
	public static final TankTintHandler INSTANCE = new TankTintHandler();

	private TankTintHandler() {

	}

	@Override
	public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
		int variant = CharsetStorageTanks.tankBlock.getVariant(worldIn, pos);
		if (variant > 0 && variant <= 16) {
			return ColorUtils.toIntColor(EnumDyeColor.byMetadata(variant - 1));
		} else {
			return -1;
		}
	}

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		if (stack.getTagCompound().hasKey("color", Constants.NBT.TAG_ANY_NUMERIC)) {
			int c = stack.getTagCompound().getInteger("color");
			if (c >= 0 && c < 16) {
				return ColorUtils.toIntColor(EnumDyeColor.byMetadata(c));
			}
		}
		return -1;
	}
}
