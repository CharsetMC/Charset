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

package pl.asie.charset.lib.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class MultipartUtils {
    public static class ExtendedRayTraceResult extends RayTraceResult {
        protected TileEntity tile;
        protected boolean hasTile;

        public ExtendedRayTraceResult(RayTraceResult result) {
            super(result.typeOfHit, result.hitVec, result.sideHit, result.getBlockPos());
            this.entityHit = result.entityHit;
            this.subHit = result.subHit;
            this.hitInfo = result.hitInfo;
        }

        public ExtendedRayTraceResult(RayTraceResult result, TileEntity tile) {
            this(result);
            this.hasTile = true;
            this.tile = tile;
        }

        public TileEntity getTile(IBlockAccess world) {
            if (hasTile) {
                return tile;
            } else if (typeOfHit == Type.BLOCK) {
                return world.getTileEntity(getBlockPos());
            } else {
                return null;
            }
        }
    }

    public static MultipartUtils INSTANCE = new MultipartUtils();

    protected MultipartUtils() {

    }

    public ExtendedRayTraceResult getTrueResult(RayTraceResult result) {
        return new ExtendedRayTraceResult(result);
    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, Collection<AxisAlignedBB> boxes2) {
        return boxes1.stream().anyMatch(b1 -> boxes2.stream().anyMatch(b1::intersects));
    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, AxisAlignedBB box2) {
        return boxes1.stream().anyMatch(box2::intersects);
    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, IBlockAccess world, BlockPos pos, Predicate<IBlockState> checkPredicate) {
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
            IBlockState state = world.getBlockState(pos);
            if (checkPredicate.test(state)) {
                state.addCollisionBoxToList((World) world, pos, collisionBox.offset(pos), boxes2, null, false);
            }

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
            IBlockState state = world.getBlockState(pos);
            return checkPredicate.test(state) && intersects(boxes1, state.getBoundingBox(world, pos));
        }
    }
}
