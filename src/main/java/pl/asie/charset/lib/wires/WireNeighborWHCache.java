/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.wires;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.render.model.IRenderComparable;

import java.util.Arrays;
import java.util.Objects;

public class WireNeighborWHCache implements IRenderComparable<WireNeighborWHCache> {
	private final float[] widths;
	private final float[] heights;
	private final int hash;

	public WireNeighborWHCache(IBlockAccess world, BlockPos pos, Wire wire) {
		widths = new float[6];
		heights = new float[6];

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (wire.connectsInternal(facing)) {
				Wire w = WireUtils.getWire(world, pos, WireFace.get(facing));
				if (w != null) {
					widths[facing.ordinal()] = w.getProvider().getWidth();
					heights[facing.ordinal()] = w.getProvider().getHeight();
				}
			}
		}

		hash = Objects.hash(widths, heights);
	}

	public float getWidth(EnumFacing facing) {
		return widths[facing.getIndex()];
	}

	public float getHeight(EnumFacing facing) {
		return heights[facing.getIndex()];
	}

	@Override
	public boolean renderEquals(WireNeighborWHCache other) {
		return Arrays.equals(other.widths, widths) && Arrays.equals(other.heights, heights);
	}

	@Override
	public int renderHashCode() {
		return hash;
	}
}
