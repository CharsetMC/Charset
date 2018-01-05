package pl.asie.charset.patchwork;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LaserRedstoneHook {
	public interface Handler {
		int getLaserPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side);
	}

	public static Handler handler;

	public static int getRedstonePower(World world, BlockPos pos, EnumFacing facing) {
		if (handler == null) {
			return world.getRedstonePower(pos, facing);
		}

		int l = handler.getLaserPower(world, pos, facing);
		if (l >= 15) return 15; else return Math.max(world.getRedstonePower(pos, facing), l);
	}

	public static int getStrongPower(Block block, IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (handler == null) {
			return block.getStrongPower(blockState, blockAccess, pos, side);
		}

		int l = handler.getLaserPower(blockAccess, pos, side);
		if (l >= 15) return 15; else return Math.max(block.getStrongPower(blockState, blockAccess, pos, side), l);
	}
}
