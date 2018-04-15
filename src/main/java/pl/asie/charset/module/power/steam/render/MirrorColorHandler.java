package pl.asie.charset.module.power.steam.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.utils.RenderUtils;

import javax.annotation.Nullable;

public class MirrorColorHandler implements IBlockColor, IItemColor {
    public static final MirrorColorHandler INSTANCE = new MirrorColorHandler();

    private MirrorColorHandler() {

    }

    private int colorMultiplier(@Nullable ItemStack sourceStack) {
        sourceStack = new ItemStack(Blocks.IRON_BLOCK);

        if (sourceStack != null) {
            return 0xFF000000 | RenderUtils.getAverageColor(RenderUtils.getItemSprite(sourceStack), RenderUtils.AveragingMode.FULL);
        } else {
            return 0xFFFFFFFF;
        }
    }

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 0) {
            return colorMultiplier(null);
        } else {
            return -1;
        }
    }

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return colorMultiplier(null);
        } else {
            return -1;
        }
    }
}
