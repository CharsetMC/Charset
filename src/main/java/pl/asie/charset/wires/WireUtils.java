package pl.asie.charset.wires;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

public final class WireUtils {
	private WireUtils() {

	}

	public static int getSignalLevel(IBlockAccess world, BlockPos pos, EnumFacing side) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof BlockWire) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileWire) {
				return ((TileWire) tile).getSignalLevel();
			}
		} else {
			int power = block.shouldCheckWeakPower(world, pos, side) ? block.getStrongPower(world, pos, state, side) : block.getWeakPower(world, pos, state, side);
			if (power > 0) {
				return 255;
			}
		}

		return 0;
	}
}
