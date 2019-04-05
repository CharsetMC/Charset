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

package pl.asie.charset.lib.capability.impl;

import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.api.lib.IMultiblockStructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiblockStructureChest implements IMultiblockStructure {
    private final TileEntityChest chest;

    public MultiblockStructureChest(TileEntityChest chest) {
        this.chest = chest;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        List<BlockPos> list = new ArrayList<>(2);

        list.add(chest.getPos());
        if (chest.adjacentChestXNeg != null) list.add(chest.adjacentChestXNeg.getPos());
        if (chest.adjacentChestXPos != null) list.add(chest.adjacentChestXPos.getPos());
        if (chest.adjacentChestZNeg != null) list.add(chest.adjacentChestZNeg.getPos());
        if (chest.adjacentChestZPos != null) list.add(chest.adjacentChestZPos.getPos());

        return list.iterator();
    }

    @Override
    public boolean contains(BlockPos pos) {
        if (pos.equals(chest.getPos())) {
            return true;
        }

        if (chest.adjacentChestXNeg != null && chest.adjacentChestXNeg.getPos().equals(pos)) return true;
        if (chest.adjacentChestXPos != null && chest.adjacentChestXPos.getPos().equals(pos)) return true;
        if (chest.adjacentChestZNeg != null && chest.adjacentChestZNeg.getPos().equals(pos)) return true;
        if (chest.adjacentChestZPos != null && chest.adjacentChestZPos.getPos().equals(pos)) return true;

        return false;
    }
}
