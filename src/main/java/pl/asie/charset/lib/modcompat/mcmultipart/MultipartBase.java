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

package pl.asie.charset.lib.modcompat.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.lib.block.BlockBase;

import java.util.List;

public abstract class MultipartBase implements IMultipartBase {
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IPartInfo part, int fortune) {
        NonNullList<ItemStack> drops = NonNullList.create();
        if (part.getState().getBlock() instanceof BlockBase) {
            ((BlockBase) part.getState().getBlock()).getDrops(drops, part.getPartWorld(), part.getPartPos(), part.getState(), part.getTile() != null ? part.getTile().getTileEntity() : null, fortune, false);
        } else {
            part.getState().getBlock().getDrops(drops, part.getPartWorld(), part.getPartPos(), part.getState(), fortune);
        }
        return drops;
    }
}
