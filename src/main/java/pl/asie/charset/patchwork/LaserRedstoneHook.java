/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

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
