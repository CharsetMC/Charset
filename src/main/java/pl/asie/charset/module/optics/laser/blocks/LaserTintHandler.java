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

package pl.asie.charset.module.optics.laser.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.module.optics.laser.CharsetLaser;

import javax.annotation.Nullable;

public class LaserTintHandler implements IBlockColor, IItemColor {
	public static final LaserTintHandler INSTANCE = new LaserTintHandler();

	private LaserTintHandler() {

	}

	@Override
	public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
		if (tintIndex != 0) return 0xFFFFFFFF;
		return 0xFF000000 | CharsetLaser.LASER_COLORS[state.getValue(CharsetLaser.LASER_COLOR).ordinal()];
	}

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		if (tintIndex != 0) return 0xFFFFFFFF;
		return 0xFF000000 | CharsetLaser.LASER_COLORS[stack.getMetadata() & 7];
	}
}
