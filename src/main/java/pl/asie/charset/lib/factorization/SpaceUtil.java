/*
 * Copyright (c) 2016 neptunepink
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.factorization;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;

import javax.vecmath.Vector3d;
import java.util.*;

/**
 * Operations on AxisAlignedBB (aka 'Box'), Vec3d, EnumFacing, Entities, and conversions between them.
 */
public final class SpaceUtil {

    public static final byte GET_POINT_MIN = 0x0;
    public static final byte GET_POINT_MAX = 0x7;

    public static EnumFacing determineOrientation(EntityLivingBase player) {
        if (player.rotationPitch > 75) {
            return EnumFacing.DOWN;
        }
        if (player.rotationPitch <= -75) {
            return EnumFacing.UP;
        }
        return determineFlatOrientation(player);
    }

    public static EnumFacing determineFlatOrientation(EntityLivingBase player) {
        //stolen from BlockPistonBase.determineOrientation. It was reversed, & we handle the y-axis differently
        int var7 = MathHelper.floor_double((double) ((180 + player.rotationYaw) * 4.0F / 360.0F) + 0.5D) & 3;
        int r = var7 == 0 ? 2 : (var7 == 1 ? 5 : (var7 == 2 ? 3 : (var7 == 3 ? 4 : 0)));
        return EnumFacing.VALUES[r];
    }

    @Deprecated // newVec!
    public static Vec3d newvec() {
        return new Vec3d(0, 0, 0);
    }

    public static Vec3d copy(Vec3d a) {
        return new Vec3d(a.xCoord, a.yCoord, a.zCoord);
    }

    public static AxisAlignedBB copy(AxisAlignedBB box) {
        return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public static Vec3d fromEntPos(Entity ent) {
        return new Vec3d(ent.posX, ent.posY, ent.posZ);
    }

    public static Vec3d fromEntVel(Entity ent) {
        return new Vec3d(ent.motionX, ent.motionY, ent.motionZ);
    }

    public static void toEntVel(Entity ent, Vec3d vec) {
        ent.motionX = vec.xCoord;
        ent.motionY = vec.yCoord;
        ent.motionZ = vec.zCoord;
    }

    public static Vec3d fromPlayerEyePos(EntityPlayer ent) {
        // This is all iChun's fault. :/
        // Uh...
        if (ent.worldObj.isRemote) {
            return new Vec3d(ent.posX, ent.posY + (ent.getEyeHeight() - ent.getDefaultEyeHeight()), ent.posZ);
        } else {
            return new Vec3d(ent.posX, ent.posY + ent.getEyeHeight(), ent.posZ);
        }
    }

    /** Sets the entity's position directly. Does *NOT* update the bounding box! */
    public static void toEntPos(Entity ent, Vec3d pos) {
        ent.posX = pos.xCoord;
        ent.posY = pos.yCoord;
        ent.posZ = pos.zCoord;
    }

    /** Sets the entity's position using its setter. Will (presumably) update the bounding box. */
    public static void setEntPos(Entity ent, Vec3d pos) {
        ent.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
    }

    public static AxisAlignedBB setMin(AxisAlignedBB aabb, Vec3d v) {
        return new AxisAlignedBB(
                v.xCoord, v.yCoord, v.zCoord,
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
                v.xCoord, v.yCoord, v.zCoord);
    }

    public static Vec3d getMiddle(AxisAlignedBB ab) {
        return new Vec3d(
                (ab.minX + ab.maxX) / 2,
                (ab.minY + ab.maxY) / 2,
                (ab.minZ + ab.maxZ) / 2);
    }

    public static AxisAlignedBB incrContract(AxisAlignedBB box, double dx, double dy, double dz) {
        return box.expand(-dx, -dy, -dz);
    }

    public static Vec3d fromDirection(EnumFacing dir) {
        //return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        // TODO: NORELEASE.fixme("There may be more bad conversions like this; there is a direct coord query from EnumFacing.");
        return new Vec3d(dir.getDirectionVec().getX(), dir.getDirectionVec().getY(), dir.getDirectionVec().getZ());
    }

    /* public static SortedPair<Vec3d> sort(Vec3d left, Vec3d right) {
        double minX = Math.min(left.xCoord, right.xCoord);
        double maxX = Math.max(left.xCoord, right.xCoord);
        double minY = Math.min(left.yCoord, right.yCoord);
        double maxY = Math.max(left.yCoord, right.yCoord);
        double minZ = Math.min(left.zCoord, right.zCoord);
        double maxZ = Math.max(left.zCoord, right.zCoord);
        return new SortedPair<Vec3d>(new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ));
    } */

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
        return newBox(getVertex(box, low), getVertex(box, high));
    }

    public static double getDiagonalLength(AxisAlignedBB ab) {
        double x = ab.maxX - ab.minX;
        double y = ab.maxY - ab.minY;
        double z = ab.maxZ - ab.minZ;
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static Vec3d averageVec(Vec3d a, Vec3d b) {
        return new Vec3d((a.xCoord + b.xCoord) / 2, (a.yCoord + b.yCoord) / 2, (a.zCoord + b.zCoord) / 2);
    }


    public static double getAngle(Vec3d a, Vec3d b) {
        double dot = a.dotProduct(b);
        double mags = a.lengthVector() * b.lengthVector();
        double div = dot / mags;
        if (div > 1) div = 1;
        if (div < -1) div = -1;
        return Math.acos(div);
    }

    public static AxisAlignedBB newBox(Vec3d min, Vec3d max) {
        return new AxisAlignedBB(
                min.xCoord, min.yCoord, min.zCoord,
                max.xCoord, max.yCoord, max.zCoord);
    }

    public static AxisAlignedBB newBox(Vec3d[] parts) {
        return newBox(getLowest(parts), getHighest(parts));
    }

    public static Vec3d scale(Vec3d base, double s) {
        return new Vec3d(base.xCoord * s, base.yCoord * s, base.zCoord * s);
    }

    public static Vec3d componentMultiply(Vec3d a, Vec3d b) {
        return new Vec3d(a.xCoord + b.yCoord, a.yCoord + b.yCoord, a.zCoord + b.yCoord);
    }

    public static Vec3d componentMultiply(Vec3d a, double x, double y, double z) {
        return new Vec3d(a.xCoord + x, a.yCoord + y, a.zCoord + z);
    }

    public static AxisAlignedBB newBoxSort(Vec3d min, Vec3d max) {
        double minX = Math.min(min.xCoord, max.xCoord);
        double minY = Math.min(min.yCoord, max.yCoord);
        double minZ = Math.min(min.zCoord, max.zCoord);
        double maxX = Math.max(min.xCoord, max.xCoord);
        double maxY = Math.max(min.yCoord, max.yCoord);
        double maxZ = Math.max(min.zCoord, max.zCoord);
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static AxisAlignedBB newBoxUnsort(Vec3d min, Vec3d max) {
        return new AxisAlignedBB(
                min.xCoord, min.yCoord, min.zCoord,
                max.xCoord, max.yCoord, max.zCoord);
    }

    public static AxisAlignedBB addCoord(AxisAlignedBB box, Vec3d vec) {
        return box.addCoord(vec.xCoord, vec.yCoord, vec.zCoord);
        // NORELEASE: Is the above right? Should be equivalent to this:
        /*if (vec.xCoord < box.minX) box.minX = vec.xCoord;
        if (box.maxX < vec.xCoord) box.maxX = vec.xCoord;
        if (vec.yCoord < box.minY) box.minY = vec.yCoord;
        if (box.maxY < vec.yCoord) box.maxY = vec.yCoord;
        if (vec.zCoord < box.minZ) box.minZ = vec.zCoord;
        if (box.maxZ < vec.zCoord) box.maxZ = vec.zCoord;*/
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
                x = v.xCoord;
                y = v.yCoord;
                z = v.zCoord;
                continue;
            }
            if (v.xCoord < x) x = v.xCoord;
            if (v.yCoord < y) y = v.yCoord;
            if (v.zCoord < z) z = v.zCoord;
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
                x = v.xCoord;
                y = v.yCoord;
                z = v.zCoord;
                continue;
            }
            if (v.xCoord > x) x = v.xCoord;
            if (v.yCoord > y) y = v.yCoord;
            if (v.zCoord > z) z = v.zCoord;
        }
        return new Vec3d(x, y, z);
    }

    public static ArrayList<EnumFacing> getRandomDirections(Random rand) {
        ArrayList<EnumFacing> ret = new ArrayList<EnumFacing>(6);
        for (int i = 0; i < 6; i++) {
            ret.add(SpaceUtil.getOrientation(i));
        }
        Collections.shuffle(ret, rand);
        return ret;
    }

    public static int getAxis(EnumFacing fd) {
        if (fd.getDirectionVec().getX() != 0) {
            return 1;
        }
        if (fd.getDirectionVec().getY() != 0) {
            return 2;
        }
        if (fd.getDirectionVec().getZ() != 0) {
            return 3;
        }
        return 0;
    }

    public static boolean isZero(Vec3d vec) {
        return vec.xCoord == 0 && vec.yCoord == 0 && vec.zCoord == 0;
    }


    /**
     * Return the distance between point and the line defined as passing through the origin and lineVec
     * @param lineVec The vector defining the line, relative to the origin.
     * @param point The point being measured, relative to the origin
     * @return the distance between line defined by lineVec and point
     */
    public static double lineDistance(Vec3d lineVec, Vec3d point) {
        // http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html equation 9
        double mag = lineVec.lengthVector();
        Vec3d nPoint = scale(point, -1);
        return lineVec.crossProduct(nPoint).lengthVector() / mag;
    }

    public static EnumFacing getOrientation(int ordinal) {
        if (ordinal < 0) return null;
        if (ordinal >= 6) return null;
        return EnumFacing.VALUES[ordinal];
    }

    public static FzOrientation getOrientation(EntityLivingBase player, EnumFacing facing, Vec3d hit) {
        double u = 0.5, v = 0.5; //We pick the axiis based on which side gets clicked
        if (facing == null) facing = EnumFacing.DOWN;
        assert facing != null;
        switch (facing) {
            default:
            case DOWN:
                u = 1 - hit.xCoord;
                v = hit.zCoord;
                break;
            case UP:
                u = hit.xCoord;
                v = hit.zCoord;
                break;
            case NORTH:
                u = hit.xCoord;
                v = hit.yCoord;
                break;
            case SOUTH:
                u = 1 - hit.xCoord;
                v = hit.yCoord;
                break;
            case WEST:
                u = 1 - hit.zCoord;
                v = hit.yCoord;
                break;
            case EAST:
                u = hit.zCoord;
                v = hit.yCoord;
                break;
        }
        u -= 0.5;
        v -= 0.5;
        double angle = Math.toDegrees(Math.atan2(v, u)) + 180;
        angle = (angle + 45) % 360;
        int pointy = (int) (angle/90);
        pointy = (pointy + 1) % 4;

        FzOrientation fo = FzOrientation.fromDirection(facing);
        for (int X = 0; X < pointy; X++) {
            fo = fo.getNextRotationOnFace();
        }
        EnumFacing orient = SpaceUtil.determineOrientation(player);
        if (orient.getAxis() != EnumFacing.Axis.Y
                && facing.getAxis() == EnumFacing.Axis.Y) {
            facing = orient;
            fo = orient == null ? null : FzOrientation.fromDirection(orient.getOpposite());
            if (fo != null) {
                FzOrientation perfect = fo.pointTopTo(EnumFacing.UP);
                if (perfect != null) {
                    fo = perfect;
                }
            }
        }
        double dist = Math.max(Math.abs(u), Math.abs(v));
        if (dist < 0.33) {
            FzOrientation perfect = fo.pointTopTo(EnumFacing.UP);
            if (perfect != null) {
                fo = perfect;
            }
        }
        return fo;
    }

    public static int sign(EnumFacing dir) {
        if (dir == null) return 0;
        return dir.getAxisDirection().getOffset();
    }

    public static double sum(Vec3d vec) {
        return vec.xCoord + vec.yCoord + vec.zCoord;
    }

    public static EnumFacing round(Vec3d vec, EnumFacing not) {
        if (isZero(vec)) return null;
        Vec3i work = null;
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
                Math.floor(vec.xCoord),
                Math.floor(vec.yCoord),
                Math.floor(vec.zCoord));
    }

    public static Vec3d normalize(Vec3d v) {
        // Vanilla's threshold is too low for my purposes.
        double length = v.lengthVector();
        if (length == 0) return newvec();
        double inv = 1.0 / length;
        if (Double.isNaN(inv) || Double.isInfinite(inv)) return newvec();
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

        if (at.xCoord < minX) minX = at.xCoord;
        if (at.xCoord > maxX) maxX = at.xCoord;
        if (at.yCoord < minY) minY = at.yCoord;
        if (at.yCoord > maxY) maxY = at.yCoord;
        if (at.zCoord < minZ) minZ = at.zCoord;
        if (at.zCoord > maxZ) maxZ = at.zCoord;

        return new AxisAlignedBB(
                minX, minY, minZ,
                maxX, maxY, maxZ);
    }

    // TODO
    /*
    public static boolean contains(AxisAlignedBB box, BlockPos at) {
        return NumUtil.intersect(box.minX, box.maxX, at.getX(), at.getX() + 1)
                && NumUtil.intersect(box.minY, box.maxY, at.getY(), at.getY() + 1)
                && NumUtil.intersect(box.minZ, box.maxZ, at.getZ(), at.getZ() + 1);

    }

    public static boolean contains(AxisAlignedBB box, Coord at) {
        return NumUtil.intersect(box.minX, box.maxX, at.x, at.x + 1)
                && NumUtil.intersect(box.minY, box.maxY, at.y, at.y + 1)
                && NumUtil.intersect(box.minZ, box.maxZ, at.z, at.z + 1);
    }
    */

    public static double getVolume(AxisAlignedBB box) {
        if (box == null) return 0;
        double x = box.maxX - box.minX;
        double y = box.maxY - box.minY;
        double z = box.maxZ - box.minZ;
        double volume = x * y * z;

        if (volume < 0) return 0;
        return volume;
    }

    public static AxisAlignedBB getBox(BlockPos at, int R) {
        return new AxisAlignedBB(at.add(-R, -R, -R), at.add(+R, +R, +R));
    }

    public static Vec3d dup(double d) {
        return new Vec3d(d, d, d);
    }

    /**
     * Rotate the allowed direction that is nearest to the rotated dir.
     * @param dir The original direction
     * @param rot The rotation to apply
     * @param allow The directions that may be used.
     * @return A novel direction
     */
    public static EnumFacing rotateDirection(EnumFacing dir, Quaternion rot, Iterable<EnumFacing> allow) {
        Vec3d v = fromDirection(dir);
        rot.applyRotation(v);
        EnumFacing best = null;
        double bestDot = Double.POSITIVE_INFINITY;
        for (EnumFacing fd : allow) {
            Vec3d f = fromDirection(fd);
            rot.applyRotation(f);
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

    public static Vec3d subtract(Vec3d you, Vec3d me) {
        return you.subtract(me);
    }

    public static Vec3d setX(Vec3d v, double x) {
        return new Vec3d(x, v.yCoord, v.zCoord);
    }

    public static Vec3d setY(Vec3d v, double y) {
        return new Vec3d(v.xCoord, y, v.zCoord);
    }

    public static Vec3d setZ(Vec3d v, double z) {
        return new Vec3d(v.xCoord, v.yCoord, z);
    }

    public static EnumFacing fromAxis(EnumFacing.Axis a) {
        if (a == EnumFacing.Axis.Y) return EnumFacing.DOWN;
        if (a == EnumFacing.Axis.X) return EnumFacing.WEST;
        if (a == EnumFacing.Axis.Z) return EnumFacing.NORTH;
        return null;
    }

    public static AxisAlignedBB newBox() {
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    public static Vec3d newVec() {
        return new Vec3d(0, 0, 0);
    }

    public static BlockPos newPos() {
        return new BlockPos(0, 0, 0);
    }

    public static AxisAlignedBB newBoxAround(BlockPos pos) {
        return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
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
    // Rescued from Forge. (This is a table of simple mathematical facts and involves
    // no creativity or arrangement, therefore copyright doesn't apply. So there.)

    public static EnumFacing rotate(EnumFacing dir, EnumFacing axis) {
        // EnumFacing admittedly does have rotate methods.
        // However, I don't feel like trusting them to work the same as ForgeDirection did.
        // If this is in fact unnecessarily it'll be easy enough to inline the appropriate code.
        return EnumFacing.VALUES[ROTATION_MATRIX[axis.ordinal()][dir.ordinal()]];
    }

    public static EnumFacing rotateBack(EnumFacing dir, EnumFacing axis) {
        return rotate(rotate(rotate(dir, axis), axis), axis);
    }

    public static Iterable<BlockPos.MutableBlockPos> iteratePos(BlockPos src, int r) {
        return BlockPos.getAllInBoxMutable(src.add(-r, -r, -r), src.add(+r, +r, +r));
    }

    public static Vector3d toJavax(Vec3d val) {
        return new Vector3d(val.xCoord, val.yCoord, val.zCoord);
    }

    public static AxisAlignedBB getBox(Chunk chunk) {
        int minX = chunk.xPosition << 4;
        int minZ = chunk.zPosition << 4;
        return new AxisAlignedBB(minX, 0, minZ, minX + 16, 0xFF, minZ + 16);
    }

    public static boolean equals(AxisAlignedBB a, AxisAlignedBB b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.minX == b.minX && a.minY == b.minY && a.minZ == b.minZ
                && a.maxX == b.maxX && a.maxY == b.maxY && a.maxZ == b.maxZ;
    }

    public static boolean equals(Vec3d a, Vec3d b) {
        return a.xCoord == b.xCoord && a.yCoord == b.yCoord && a.zCoord == b.zCoord;
    }

    public static double lengthSquare(Vec3d vec) {
        return vec.xCoord * vec.xCoord
                + vec.yCoord * vec.yCoord
                + vec.zCoord * vec.zCoord;
    }
}
