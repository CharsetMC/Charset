package pl.asie.charset.lib.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public final class ShadingTintColorHandler implements IBlockColor, IItemColor {
    public static final ShadingTintColorHandler INSTANCE = new ShadingTintColorHandler();

    private ShadingTintColorHandler() {

    }

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        return ((tintIndex & 0xFF) * 0x10101) | 0xFF000000;
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        return ((tintIndex & 0xFF) * 0x10101) | 0xFF000000;
    }
}
