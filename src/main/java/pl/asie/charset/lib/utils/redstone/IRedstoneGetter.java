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

package pl.asie.charset.lib.utils.redstone;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.function.Predicate;

public interface IRedstoneGetter {
    /**
     * @return -1 = ignore result (use next handler), 0-15 - redstone signal value
     */
    int get(IBlockAccess world, BlockPos pos, EnumFacing face, @Nullable EnumFacing edge, Predicate<TileEntity> tileEntityPredicate);

    /**
     * @return null = ignore result (use next handler), byte[16] - bundled signal value
     */
    byte[] getBundled(IBlockAccess world, BlockPos pos, EnumFacing face, @Nullable EnumFacing edge, Predicate<TileEntity> tileEntityPredicate);
}
