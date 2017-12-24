/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.laser.blocks;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.module.laser.CharsetLaser;
import pl.asie.charset.api.laser.ILaserBeamFactory;
import pl.asie.charset.api.laser.ILaserSource;
import pl.asie.charset.api.laser.LaserColor;

import javax.annotation.Nullable;

public class TileLaserSourceBase extends TileBase {
	protected final LaserColor[] colors = new LaserColor[6];
	private final ILaserSource[] sources = new ILaserSource[6];

	public TileLaserSourceBase() {
		for (int i = 0; i < 6; i++) {
			colors[i] = LaserColor.NONE;
			sources[i] = createLaserSource(i);
		}
	}

	protected ILaserSource createLaserSource(int i) {
		return new ILaserSource.Tile(this) {
			@Override
			public void updateBeam(ILaserBeamFactory factory) {
				if (colors[i] == LaserColor.NONE) {
					beam = null;
				} else if (beam == null || !beam.isValid(getWorld(), getPos()) || beam.getColor() != colors[i]) {
					beam = factory.create(TileLaserSourceBase.this, EnumFacing.getFront(i), colors[i]);
				}
			}
		};
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (int i = 0; i < 6; i++) {
			colors[i] = LaserColor.NONE;
		}
	}

	@Override
	public void onLoad() {
		CharsetLaser.laserStorage.registerLaserSources(getWorld(), getPos());
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return (facing != null && capability == CharsetLaser.LASER_SOURCE) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
		if (facing != null) {
			if (capability == CharsetLaser.LASER_SOURCE) {
				return CharsetLaser.LASER_SOURCE.cast(sources[facing.ordinal()]);
			}
		}

		return super.getCapability(capability, facing);
	}
}
