/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class RedstoneUtils {
	private RedstoneUtils() {

	}

	public static int getRedstonePowerWithWire(World world, BlockPos pos, EnumFacing facing) {
		IBlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if (block instanceof BlockRedstoneWire) {
			return iblockstate.getValue(BlockRedstoneWire.POWER);
		}

		return block.shouldCheckWeakPower(iblockstate, world, pos, facing) ? world.getStrongPower(pos) : iblockstate.getWeakPower(world, pos, facing);
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

		/* IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container != null) {
			return MultipartRedstoneHelper.canConnectRedstone(container, side, face);
		} else { */
			return block.canConnectRedstone(state, world, pos, side);
		// }
	}
}
