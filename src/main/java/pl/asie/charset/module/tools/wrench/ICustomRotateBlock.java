package pl.asie.charset.module.tools.wrench;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface ICustomRotateBlock {
    boolean rotateBlock(World world, BlockPos pos, IBlockState state, EnumFacing axis);
}
