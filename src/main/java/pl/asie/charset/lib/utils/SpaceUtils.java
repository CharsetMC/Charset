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

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Operations on AxisAlignedBB (aka 'Box'), Vec3d, EnumFacing, Entities, and conversions between them.
 */
public final class SpaceUtils {

    public static final byte GET_POINT_MIN = 0x0;
    public static final byte GET_POINT_MAX = 0x7;

    public static Vec3d getCenter(AxisAlignedBB bb) {
        return new Vec3d(
                bb.minX + ((bb.maxX - bb.minX) / 2.0),
                bb.minY + ((bb.maxY - bb.minY) / 2.0),
                bb.minZ + ((bb.maxZ - bb.minZ) / 2.0)
        );
    }

    public static EnumFacing determineOrientation(EntityLivingBase player) {
        if (player.rotationPitch > 75) {
            return EnumFacing.DOWN;
        } else if (player.rotationPitch <= -75) {
            return EnumFacing.UP;
        } else {
            return determineFlatOrientation(player);
        }
    }

    // TODO: Rename?
    public static EnumFacing determineFlatOrientation(EntityLivingBase player) {
        int var7 = MathHelper.floor((double) ((180 + player.rotationYaw) * 4.0F / 360.0F) + 0.5D) & 3;
        int r = var7 == 0 ? 2 : (var7 == 1 ? 5 : (var7 == 2 ? 3 : (var7 == 3 ? 4 : 0)));
        return EnumFacing.VALUES[r];
    }

    public static Vec3d copy(Vec3d a) {
        return new Vec3d(a.x, a.y, a.z);
    }

    public static AxisAlignedBB copy(AxisAlignedBB box) {
        return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public static Vec3d getEntityVelocity(Entity ent) {
        return new Vec3d(ent.motionX, ent.motionY, ent.motionZ);
    }

    public static void setEntityVelocity(Entity ent, Vec3d vec) {
        ent.motionX = vec.x;
        ent.motionY = vec.y;
        ent.motionZ = vec.z;
    }

    public static int ordinal(@Nullable EnumFacing side) {
        return side == null ? 6 : side.ordinal();
    }

    @Nullable
    public static EnumFacing getFacing(int ordinal) {
        return ordinal == 6 ? null : EnumFacing.byIndex(ordinal);
    }

    public static Vec3d fromPlayerEyePos(EntityPlayer ent) {
        // This is all iChun's fault. :/
        // Uh...
        if (ent.world.isRemote) {
            return new Vec3d(ent.posX, ent.posY + (ent.getEyeHeight() - ent.getDefaultEyeHeight()), ent.posZ);
        } else {
            return new Vec3d(ent.posX, ent.posY + ent.getEyeHeight(), ent.posZ);
        }
    }

    /** Sets the entity's position directly. Does *NOT* update the bounding box! */
    public static void setEntityPosition(Entity ent, Vec3d pos) {
        ent.posX = pos.x;
        ent.posY = pos.y;
        ent.posZ = pos.z;
    }

    /** Sets the entity's position using its setter. Will (presumably) update the bounding box. */
    public static void setEntPos(Entity ent, Vec3d pos) {
        ent.setPosition(pos.x, pos.y, pos.z);
    }

    public static AxisAlignedBB setMin(AxisAlignedBB aabb, Vec3d v) {
        return new AxisAlignedBB(
                v.x, v.y, v.z,
                aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    public static Vec3d getMax(AxisAlignedBB aabb) {
        return new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    public static Vec3d getMin(AxisAlignedBB aabb) {
        return new Vec3d(aabb.minX, aabb.minY, aabb.minZ);
    }

    public static AxisAlignedBB setMax(AxisAlignedBB aabb, Vec3d v) {
        return new AxisAlignedBB(
                aabb.minX, aabb.minY, aabb.minZ,
                v.x, v.y, v.z);
    }

    public static Vec3d getMiddle(AxisAlignedBB ab) {
        return new Vec3d(
                (ab.minX + ab.maxX) / 2,
                (ab.minY + ab.maxY) / 2,
                (ab.minZ + ab.maxZ) / 2);
    }

    public static Vec3d fromDirection(EnumFacing dir) {
        return new Vec3d(dir.getDirectionVec());
    }

    public static Pair<Vec3d, Vec3d> sort(Vec3d left, Vec3d right) {
        double minX = Math.min(left.x, right.x);
        double maxX = Math.max(left.x, right.x);
        double minY = Math.min(left.y, right.y);
        double maxY = Math.max(left.y, right.y);
        double minZ = Math.min(left.z, right.z);
        double maxZ = Math.max(left.z, right.z);
        return Pair.of(new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ));
    }

    /**
     * Copies a point on box into target.
     * pointFlags is a bit-flag, like <Z, Y, X>.
     * So if the value is 0b000, then target is the minimum point,
     * and 0b111 the target is the maximum.
     */
    public static Vec3d getVertex(AxisAlignedBB box, byte pointFlags) {
        boolean xSide = (pointFlags & 1) == 1;
        boolean ySide = (pointFlags & 2) == 2;
        boolean zSide = (pointFlags & 4) == 4;
        return new Vec3d(
                xSide ? box.minX : box.maxX,
                ySide ? box.minY : box.maxY,
                zSide ? box.minZ : box.maxZ
        );
    }

    /**
     * @param box The box to be flattened
     * @param face The side of the box that will remain untouched; the opposite face will be brought to it
     * @return A new box, with a volume of 0. Returns null if face is invalid.
     */
    public static AxisAlignedBB flatten(AxisAlignedBB box, EnumFacing face) {
        byte[] lows = new byte[] { 0x2, 0x0, 0x4, 0x0, 0x1, 0x0 };
        byte[] hghs = new byte[] { 0x7, 0x5, 0x7, 0x3, 0x7, 0x6 };
        byte low = lows[face.ordinal()];
        byte high = hghs[face.ordinal()];
        assert low != high;
        assert (~low & 0x7) != high;
        return SpaceUtils.from(getVertex(box, low), getVertex(box, high));
    }

    public static double getDiagonalLength(AxisAlignedBB ab) {
        double x = ab.maxX - ab.minX;
        double y = ab.maxY - ab.minY;
        double z = ab.maxZ - ab.minZ;
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static Vec3d average(Vec3d a, Vec3d b) {
        return new Vec3d((a.x + b.x) / 2, (a.y + b.y) / 2, (a.z + b.z) / 2);
    }

    public static double getAngle(Vec3d a, Vec3d b) {
        double dot = a.dotProduct(b);
        double mags = a.lengthSquared();
        double div = dot / mags;
        if (div > 1) div = 1;
        if (div < -1) div = -1;
        return Math.acos(div);
    }

    public static AxisAlignedBB from(Vec3d min, Vec3d max) {
        return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public static AxisAlignedBB withPoints(Vec3d[] parts) {
        return SpaceUtils.from(getLowest(parts), getHighest(parts));
    }

    public static Vec3d scale(Vec3d base, double s) {
        return new Vec3d(base.x * s, base.y * s, base.z * s);
    }

    public static Vec3d componentMultiply(Vec3d a, Vec3d b) {
        return new Vec3d(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vec3d componentMultiply(Vec3d a, double x, double y, double z) {
        return new Vec3d(a.x + x, a.y + y, a.z + z);
    }

    public static AxisAlignedBB sortedBox(Vec3d min, Vec3d max) {
        double minX = Math.min(min.x, max.x);
        double minY = Math.min(min.y, max.y);
        double minZ = Math.min(min.z, max.z);
        double maxX = Math.max(min.x, max.x);
        double maxY = Math.max(min.y, max.y);
        double maxZ = Math.max(min.z, max.z);
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static AxisAlignedBB withPoint(AxisAlignedBB box, Vec3d vec) {
        return new AxisAlignedBB(
                vec.x < box.minX ? vec.x : box.minX,
                vec.y < box.minY ? vec.y : box.minY,
                vec.z < box.minZ ? vec.z : box.minZ,
                box.maxX < vec.x ? vec.x : box.maxX,
                box.maxY < vec.y ? vec.y : box.maxY,
                box.maxZ < vec.z ? vec.z : box.maxZ
        );
    }

    public static Vec3d[] getCorners(AxisAlignedBB box) {
        return new Vec3d[]{
                new Vec3d(box.minX, box.minY, box.minZ),
                new Vec3d(box.minX, box.maxY, box.minZ),
                new Vec3d(box.maxX, box.maxY, box.minZ),
                new Vec3d(box.maxX, box.minY, box.minZ),

                new Vec3d(box.minX, box.minY, box.maxZ),
                new Vec3d(box.minX, box.maxY, box.maxZ),
                new Vec3d(box.maxX, box.maxY, box.maxZ),
                new Vec3d(box.maxX, box.minY, box.maxZ)
        };
    }

    public static Vec3d getLowest(Vec3d[] vs) {
        double x, y, z;
        x = y = z = 0;
        boolean first = true;
        for (int i = 0; i < vs.length; i++) {
            Vec3d v = vs[i];
            if (v == null) continue;
            if (first) {
                first = false;
                x = v.x;
                y = v.y;
                z = v.z;
                continue;
            }
            if (v.x < x) x = v.x;
            if (v.y < y) y = v.y;
            if (v.z < z) z = v.z;
        }
        return new Vec3d(x, y, z);
    }

    public static Vec3d getHighest(Vec3d[] vs) {
        double x, y, z;
        x = y = z = 0;
        boolean first = true;
        for (int i = 0; i < vs.length; i++) {
            Vec3d v = vs[i];
            if (v == null) continue;
            if (first) {
                first = false;
                x = v.x;
                y = v.y;
                z = v.z;
                continue;
            }
            if (v.x > x) x = v.x;
            if (v.y > y) y = v.y;
            if (v.z > z) z = v.z;
        }
        return new Vec3d(x, y, z);
    }

    public static boolean isZero(Vec3d vec) {
        return vec.x == 0 && vec.y == 0 && vec.z == 0;
    }

    /**
     * Return the distance between point and the line defined as passing through the origin and lineVec
     * @param lineVec The vector defining the line, relative to the origin.
     * @param point The point being measured, relative to the origin
     * @return the distance between line defined by lineVec and point
     */
    public static double lineDistance(Vec3d lineVec, Vec3d point) {
        // http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html equation 9
        double mag = lineVec.length();
        Vec3d nPoint = scale(point, -1);
        return lineVec.crossProduct(nPoint).length() / mag;
    }

    public static double lineDistance(Vec3d origin, Vec3d lineVec, Vec3d point) {
        return lineDistance(lineVec.subtract(origin), point.subtract(origin));
    }

    public static EnumFacing getOrientation(int ordinal) {
        if (ordinal < 0) return null;
        if (ordinal >= 6) return null;
        return EnumFacing.VALUES[ordinal];
    }

    public static Orientation getOrientation(World world, BlockPos pos, EntityLivingBase placer, EnumFacing face, float hitX, float hitY, float hitZ) {
        Vec3d hitVec = null;
        if (face == null) {
            RayTraceResult hit = RayTraceUtils.getCollision(world, pos, placer, Block.FULL_BLOCK_AABB, true);
            if (hit != null) {
                face = hit.sideHit;
                hitVec = hit.hitVec != null ? hit.hitVec.subtract(new Vec3d(pos)) : null;
            }
        } else {
            hitVec = new Vec3d(hitX, hitY, hitZ);
        }

        if (hitVec != null) {
            return SpaceUtils.getOrientation(pos, placer, face, hitVec);
        } else if (face != null) {
            return Orientation.fromDirection(face);
        } else {
            return Orientation.FACE_UP_POINT_NORTH;
        }
    }

    public static Orientation getOrientation(BlockPos pos, EntityLivingBase player, EnumFacing facing, Vec3d hit) {
        double u, v;
        if (facing == null) {
            facing = EnumFacing.DOWN;
        }
        switch (facing) {
            default:
            case DOWN:
                u = 1 - hit.x;
                v = hit.z;
                break;
            case UP:
                u = hit.x;
                v = hit.z;
                break;
            case NORTH:
                u = hit.x;
                v = hit.y;
                break;
            case SOUTH:
                u = 1 - hit.x;
                v = hit.y;
                break;
            case WEST:
                u = 1 - hit.z;
                v = hit.y;
                break;
            case EAST:
                u = hit.z;
                v = hit.y;
                break;
        }
        u -= 0.5;
        v -= 0.5;
        double angle = Math.toDegrees(Math.atan2(v, u)) + 180;
        angle = (angle + 45) % 360;
        int pointy = (int) (angle/90);
        pointy = (pointy + 1) % 4;

        Orientation fo = Orientation.fromDirection(facing);
        for (int X = 0; X < pointy; X++) {
            fo = fo.getNextRotationOnFace();
        }
        EnumFacing orient = EnumFacing.getDirectionFromEntityLiving(pos, player);
        if (orient.getAxis() != EnumFacing.Axis.Y
                && facing.getAxis() == EnumFacing.Axis.Y) {
            facing = orient;
            fo = orient == null ? null : Orientation.fromDirection(orient);
            if (fo != null) {
                Orientation perfect = fo.pointTopTo(EnumFacing.UP);
                if (perfect != null) {
                    fo = perfect;
                }
            }
        }
        double dist = Math.max(Math.abs(u), Math.abs(v));
        if (dist < 0.33) {
            Orientation perfect = fo.pointTopTo(EnumFacing.UP);
            if (perfect != null) {
                fo = perfect;
            }
        }
        return fo;
    }

    public static int sign(EnumFacing dir) {
        return dir != null ? dir.getAxisDirection().getOffset() : 0;
    }

    public static double componentSum(Vec3d vec) {
        return vec.x + vec.y + vec.z;
    }

    public static EnumFacing getClosestDirection(Vec3d vec) {
        return getClosestDirection(vec, null);
    }

    public static EnumFacing getClosestDirection(Vec3d vec, EnumFacing not) {
        if (isZero(vec)) return null;
        Vec3i work;
        double bestAngle = Double.POSITIVE_INFINITY;
        EnumFacing closest = null;
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (dir == not) continue;
            work = dir.getDirectionVec();
            double dot = getAngle(vec, new Vec3d(work));
            if (dot < bestAngle) {
                bestAngle = dot;
                closest = dir;
            }
        }
        return closest;
    }

    public static Vec3d floor(Vec3d vec) {
        return new Vec3d(
                Math.floor(vec.x),
                Math.floor(vec.y),
                Math.floor(vec.z));
    }

    public static Vec3d normalize(Vec3d v) {
        // Vanilla's threshold is too low for my purposes.
        double length = v.length();
        if (length == 0) return Vec3d.ZERO;
        double inv = 1.0 / length;
        if (Double.isNaN(inv) || Double.isInfinite(inv)) return Vec3d.ZERO;
        return scale(v, inv);
    }

    public static AxisAlignedBB include(AxisAlignedBB box, BlockPos at) {
        double minX = box.minX;
        double maxX = box.maxX;
        double minY = box.minY;
        double maxY = box.maxY;
        double minZ = box.minZ;
        double maxZ = box.maxZ;

        if (at.getX() < minX) minX = at.getX();
        if (at.getX() + 1 > maxX) maxX = at.getX() + 1;
        if (at.getY() < minY) minY = at.getY();
        if (at.getY() + 1 > maxY) maxY = at.getY() + 1;
        if (at.getZ() < minZ) minZ = at.getZ();
        if (at.getZ() + 1 > maxZ) maxZ = at.getZ() + 1;

        return new AxisAlignedBB(
                minX, minY, minZ,
                maxX, maxY, maxZ);
    }

    public static AxisAlignedBB include(AxisAlignedBB box, Vec3d at) {
        double minX = box.minX;
        double maxX = box.maxX;
        double minY = box.minY;
        double maxY = box.maxY;
        double minZ = box.minZ;
        double maxZ = box.maxZ;

        if (at.x < minX) minX = at.x;
        if (at.x > maxX) maxX = at.x;
        if (at.y < minY) minY = at.y;
        if (at.y > maxY) maxY = at.y;
        if (at.z < minZ) minZ = at.z;
        if (at.z > maxZ) maxZ = at.z;

        return new AxisAlignedBB(
                minX, minY, minZ,
                maxX, maxY, maxZ);
    }

    public static double getVolume(AxisAlignedBB box) {
        if (box != null) {
            double x = box.maxX - box.minX;
            double y = box.maxY - box.minY;
            double z = box.maxZ - box.minZ;
            double volume = x * y * z;

            return volume > 0.0D ? volume : 0.0D;
        } else {
            return 0.0D;
        }
    }

    public static AxisAlignedBB createBox(BlockPos at, int radius) {
        return new AxisAlignedBB(at.add(-radius, -radius, -radius), at.add(+radius+1, +radius+1, +radius+1));
    }

    /**
     * Rotate the allowed direction that is nearest to the rotated dir.
     * @param dir The original direction
     * @param rot The rotation to apply
     * @param allow The directions that may be used.
     * @return A novel direction
     */
    public static EnumFacing rotateDirection(EnumFacing dir, Quaternion rot, Iterable<EnumFacing> allow) {
        Vec3d v = rot.applyRotation(fromDirection(dir));
        EnumFacing best = null;
        double bestDot = Double.POSITIVE_INFINITY;
        for (EnumFacing fd : allow) {
            Vec3d f = rot.applyRotation(fromDirection(fd));
            f = rot.applyRotation(f);
            double dot = v.dotProduct(f);
            if (dot < bestDot) {
                bestDot = dot;
                best = fd;
            }
        }
        return best;
    }

    public static EnumFacing rotateDirectionAndExclude(EnumFacing dir, Quaternion rot, Collection<EnumFacing> allow) {
        EnumFacing ret = rotateDirection(dir, rot, allow);
        allow.remove(ret);
        allow.remove(ret.getOpposite());
        return ret;
    }

    private static final int[][] ROTATION_MATRIX = {
            {0, 1, 4, 5, 3, 2},
            {0, 1, 5, 4, 2, 3},
            {5, 4, 2, 3, 0, 1},
            {4, 5, 2, 3, 1, 0},
            {2, 3, 1, 0, 4, 5},
            {3, 2, 0, 1, 4, 5},
            {0, 1, 2, 3, 4, 5}
    };

    private static final int[][] ROTATION_MATRIX_INV = new int[6][6];

    static {
        for (int axis = 0; axis < 6; axis++)
            for (int dir = 0; dir < 6; dir++) {
                int out = dir;
                out = ROTATION_MATRIX[axis][out];
                out = ROTATION_MATRIX[axis][out];
                out = ROTATION_MATRIX[axis][out];
                ROTATION_MATRIX_INV[axis][dir] = out;
            }
    }

    public static EnumFacing rotateCounterclockwise(EnumFacing dir, EnumFacing axis) {
        return EnumFacing.VALUES[ROTATION_MATRIX[axis.ordinal()][dir.ordinal()]];
    }

    public static EnumFacing rotateClockwise(EnumFacing dir, EnumFacing axis) {
        return EnumFacing.VALUES[ROTATION_MATRIX_INV[axis.ordinal()][dir.ordinal()]];
    }

    public static Iterable<BlockPos.MutableBlockPos> iterateAround(BlockPos src, int radius) {
        return BlockPos.getAllInBoxMutable(src.add(-radius, -radius, -radius), src.add(+radius, +radius, +radius));
    }

    public static Vector3d toJavaVector(Vec3d val) {
        return new Vector3d(val.x, val.y, val.z);
    }

    public static AxisAlignedBB getChunkBoundingBox(Chunk chunk) {
        int minX = chunk.x << 4;
        int minZ = chunk.z << 4;
        return new AxisAlignedBB(minX, 0, minZ, minX + 16, chunk.getWorld().getActualHeight(), minZ + 16);
    }
}
