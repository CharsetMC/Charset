package pl.asie.charset.module.tweaks.remove;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPortalBlocked extends BlockPortal {
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public boolean trySpawnPortal(World worldIn, BlockPos pos) {
        return false;
    }
}
