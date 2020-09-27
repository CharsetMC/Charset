/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.optics.laser.system;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.laser.ILaserBeam;
import pl.asie.charset.api.laser.ILaserBeamFactory;
import pl.asie.charset.api.laser.ILaserSource;
import pl.asie.charset.api.laser.LaserColor;
import pl.asie.charset.module.optics.laser.CharsetLaser;

public final class LaserBeamFactory implements ILaserBeamFactory {
	public static final LaserBeamFactory INSTANCE = new LaserBeamFactory();

	private LaserBeamFactory() {

	}

	@Override
	public ILaserBeam create(TileEntity tile, EnumFacing facing, LaserColor color) {
		return new LaserBeam(tile.getCapability(CharsetLaser.LASER_SOURCE, facing), tile.getWorld(), tile.getPos(), facing, color);
	}

	@Override
	public ILaserBeam create(ILaserSource source, World world, BlockPos pos, EnumFacing facing, LaserColor color) {
		return new LaserBeam(source, world, pos, facing, color);
	}
}
