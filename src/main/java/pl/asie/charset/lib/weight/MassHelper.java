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

package pl.asie.charset.lib.weight;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.lib.utils.SpaceUtils;

public class MassHelper {
	public static MassHelper INSTANCE = new MassHelper();

	protected MassHelper() {

	}

	public double getDensity(World world, BlockPos pos) {
		return getDensity(world, pos, world.getBlockState(pos));
	}

	public double getDensity(World world, BlockPos pos, IBlockState state) {
		return state.getBlockHardness(world, pos);
	}

	public double getMass(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		double density = getDensity(world, pos, state);
		AxisAlignedBB box = state.getCollisionBoundingBox(world, pos);
		if (box == null) {
			return 0;
		} else {
			return SpaceUtils.getVolume(box) * density;
		}
	}
}
