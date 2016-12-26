package pl.asie.charset.lib.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;

public abstract class ModelColorHandler<T extends IRenderComparable<T>> implements IBlockColor, IItemColor {
    private final ModelFactory<T> parent;

    public ModelColorHandler(ModelFactory<T> parent) {
        this.parent = parent;
    }

    public abstract int colorMultiplier(T info, int tintIndex);

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (state instanceof IExtendedBlockState) {
            T info = ((IExtendedBlockState) state).getValue(parent.getProperty());
            if (info != null) {
                return colorMultiplier(info, tintIndex);
            }
        }
        return -1;
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        T info = parent.fromItemStack(stack);
        if (info != null) {
            return colorMultiplier(info, tintIndex);
        }
        return -1;
    }
}
