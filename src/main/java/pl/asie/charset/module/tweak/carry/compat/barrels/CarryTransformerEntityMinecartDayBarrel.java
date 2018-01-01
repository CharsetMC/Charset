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

package pl.asie.charset.module.tweak.carry.compat.barrels;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.module.storage.barrels.CharsetStorageBarrels;
import pl.asie.charset.module.storage.barrels.EntityMinecartDayBarrel;
import pl.asie.charset.module.storage.barrels.TileEntityDayBarrel;
import pl.asie.charset.module.tweak.carry.CarryTransformerEntityMinecart;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CarryTransformerEntityMinecartDayBarrel extends CarryTransformerEntityMinecart {
	@Nullable
	@Override
	protected Pair<IBlockState, TileEntity> getExtractedPair(@Nonnull Entity object, boolean simulate) {
		if (object instanceof EntityMinecartDayBarrel) {
			return Pair.of(CharsetStorageBarrels.barrelBlock.getDefaultState(), ((EntityMinecartDayBarrel) object).getTileInternal());
		} else {
			return null;
		}
	}

	@Override
	public boolean insert(@Nonnull Entity object, @Nonnull IBlockState state, @Nullable TileEntity tile, boolean simulate) {
		if (state.getBlock() == CharsetStorageBarrels.barrelBlock) {
			Entity out = transform(object, EntityMinecartDayBarrel.class, simulate);
			if (out != null) {
				if (!simulate) {
					((EntityMinecartDayBarrel) out).initFromTile((TileEntityDayBarrel) tile);
				}
				return true;
			}
		}

		return false;
	}
}
