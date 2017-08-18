package pl.asie.charset.api.lib;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

@FunctionalInterface
public interface IBlockCapabilityProvider<T> {
    T create(IBlockAccess world, BlockPos pos, IBlockState state);
}
