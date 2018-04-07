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

package pl.asie.charset.lib.capability;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileCache {
    protected final World world;
    protected final BlockPos pos;
    protected IBlockState state;
    protected boolean hasTile;
    private TileEntity tile;

    public TileCache(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public void neighborChanged(BlockPos pos) {
        if (this.pos.equals(pos)) {
            state = null;
        }
    }

    protected void reload() {
        state = world.getBlockState(pos);
        hasTile = state.getBlock().hasTileEntity(state);
        tile = null;
    }

    protected boolean isValid() {
        if (state == null) {
            return false;
        }

        if (hasTile) {
            if (tile == null) {
                tile = world.getTileEntity(pos);
                if (tile == null) {
                    return false;
                }
            }

            return !tile.isInvalid();
        }

        return true;
    }

    @Nonnull
    public IBlockState getBlock() {
        if (!isValid()) {
            reload();
        }

        return state;
    }

    @Nullable
    public TileEntity getTile() {
        if (!isValid()) {
            reload();
        }

        if (hasTile) {
            if (tile == null) {
                tile = world.getTileEntity(pos);
            }
            return tile;
        } else {
            return null;
        }
    }
}
