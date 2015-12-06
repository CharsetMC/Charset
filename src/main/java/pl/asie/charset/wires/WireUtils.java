package pl.asie.charset.wires;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class WireUtils {
	private static final Set<Block> WIRE_PLACEABLE = new HashSet<Block>();

	static {
		WIRE_PLACEABLE.add(Blocks.glowstone);
		WIRE_PLACEABLE.add(Blocks.piston);
		WIRE_PLACEABLE.add(Blocks.sticky_piston);
	}

	private WireUtils() {

	}

	public static boolean canPlaceWire(World world, BlockPos pos, EnumFacing side) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (WIRE_PLACEABLE.contains(state.getBlock())) {
			return true;
		}

		return block.isSideSolid(world, pos, side);
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
