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

package pl.asie.charset.module.laser.system;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.module.laser.CharsetLaser;
import pl.asie.charset.patchwork.LaserRedstoneHook;

public class LaserRedstoneHelper implements LaserRedstoneHook.Handler {
	@Override
	public int getLaserPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (blockAccess instanceof World) {
			pos = pos.offset(side.getOpposite());

			IBlockState state = blockAccess.getBlockState(pos);
			if (state.getBlock().hasTileEntity(state)) {
				TileEntity tile = blockAccess.getTileEntity(pos);
				if (tile != null && tile.hasCapability(CharsetLaser.LASER_RECEIVER, side.getOpposite())) {
					return 0;
				}
			}

			if (CharsetLaser.laserStorage.isEndpointHit((World) blockAccess, pos, side.getOpposite())) {
				return 15;
			}
		}

		return 0;
	}
}
