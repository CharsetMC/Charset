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

package pl.asie.charset.lib.utils;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OcclusionUtils {
    public static OcclusionUtils INSTANCE = new OcclusionUtils();

    protected OcclusionUtils() {

    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, Collection<AxisAlignedBB> boxes2) {
        return boxes1.stream().anyMatch(b1 -> boxes2.stream().anyMatch(b1::intersects));
    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, AxisAlignedBB box2) {
        return boxes1.stream().anyMatch(box2::intersects);
    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, IBlockAccess world, BlockPos pos) {
        if (boxes1.isEmpty()) {
            return false;
        }

        if (world instanceof World) {
            List<AxisAlignedBB> boxes2 = new ArrayList<>();
            AxisAlignedBB collisionBox = null;
            for (AxisAlignedBB box : boxes1) {
                if (collisionBox == null) {
                    collisionBox = box;
                } else {
                    collisionBox = collisionBox.union(box);
                }
            }

            // TODO: Is false right?
            world.getBlockState(pos).addCollisionBoxToList((World) world, pos, collisionBox.offset(pos), boxes2, null, false);
            if (boxes2.size() > 0) {
                BlockPos negPos = new BlockPos(-pos.getX(), -pos.getY(), -pos.getZ());
                for (int i = 0; i < boxes2.size(); i++) {
                    boxes2.set(i, boxes2.get(i).offset(negPos));
                }

                return intersects(boxes1, boxes2);
            } else {
                return false;
            }
        } else {
            return intersects(boxes1, world.getBlockState(pos).getBoundingBox(world, pos));
        }
    }
}
