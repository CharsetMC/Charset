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

package pl.asie.charset.lib.capability.impl;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.lib.IMultiblockStructure;

import java.util.Iterator;

public class MultiblockStructureBed implements IMultiblockStructure {
    private final TileEntityBed bed;

    public MultiblockStructureBed(TileEntityBed bed) {
        this.bed = bed;
    }

    private BlockPos getNeighborPos() {
        World world = bed.getWorld();
        IBlockState state = world.getBlockState(bed.getPos());
        EnumFacing facing = state.getValue(BlockBed.FACING);

        if (state.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) return bed.getPos().offset(facing);
        else return bed.getPos().offset(facing.getOpposite());
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return ImmutableList.of(bed.getPos(), getNeighborPos()).iterator();
    }

    @Override
    public boolean contains(BlockPos pos) {
        return pos.equals(bed.getPos()) || pos.equals(getNeighborPos());
    }

    @Override
    public boolean isSeparable() {
        return false;
    }
}
