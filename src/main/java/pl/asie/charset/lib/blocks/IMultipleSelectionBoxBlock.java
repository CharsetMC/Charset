package pl.asie.charset.lib.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface IMultipleSelectionBoxBlock {
	void addSelectionBoxesToList(IBlockState state, World world, BlockPos pos, List<AxisAlignedBB> selectionBoxes, Entity lookingEntity);
}
