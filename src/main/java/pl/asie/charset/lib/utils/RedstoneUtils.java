package pl.asie.charset.lib.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.MultipartRedstoneHelper;

/**
 * Created by asie on 1/6/16.
 */
public final class RedstoneUtils {
	private RedstoneUtils() {

	}

	public static int getRedstonePowerWithWire(World world, BlockPos pos, EnumFacing facing) {
		IBlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if (block instanceof BlockRedstoneWire) {
			return iblockstate.getValue(BlockRedstoneWire.POWER);
		}

		return block.shouldCheckWeakPower(world, pos, facing) ? world.getStrongPower(pos) : block.getWeakPower(world, pos, iblockstate, facing);
	}

	public static boolean canConnectFace(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side, EnumFacing face) {
		Block block = state.getBlock();
		if ((block instanceof BlockRedstoneDiode || block instanceof BlockRedstoneWire || block instanceof BlockDaylightDetector || block instanceof BlockBasePressurePlate) && face != EnumFacing.DOWN) {
			return false;
		}

		if (block instanceof BlockLever && face != state.getValue(BlockLever.FACING).getFacing().getOpposite()) {
			return false;
		}

		if (block instanceof BlockButton && face != state.getValue(BlockButton.FACING).getOpposite()) {
			return false;
		}

		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container != null) {
			return MultipartRedstoneHelper.canConnectRedstone(container, side, face);
		} else {
			return block.canConnectRedstone(world, pos, side);
		}
	}
}
