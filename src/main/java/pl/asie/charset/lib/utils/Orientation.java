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

package pl.asie.charset.lib.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public enum Orientation implements IStringSerializable {
    FACE_DOWN_POINT_SOUTH(EnumFacing.DOWN, EnumFacing.SOUTH),
    FACE_DOWN_POINT_NORTH(EnumFacing.DOWN, EnumFacing.NORTH),
    FACE_DOWN_POINT_EAST(EnumFacing.DOWN, EnumFacing.EAST),
    FACE_DOWN_POINT_WEST(EnumFacing.DOWN, EnumFacing.WEST),

    FACE_UP_POINT_SOUTH(EnumFacing.UP, EnumFacing.SOUTH),
    FACE_UP_POINT_NORTH(EnumFacing.UP, EnumFacing.NORTH),
    FACE_UP_POINT_EAST(EnumFacing.UP, EnumFacing.EAST),
    FACE_UP_POINT_WEST(EnumFacing.UP, EnumFacing.WEST),

    FACE_NORTH_POINT_UP(EnumFacing.NORTH, EnumFacing.UP),
    FACE_NORTH_POINT_DOWN(EnumFacing.NORTH, EnumFacing.DOWN),
    FACE_NORTH_POINT_EAST(EnumFacing.NORTH, EnumFacing.EAST),
    FACE_NORTH_POINT_WEST(EnumFacing.NORTH, EnumFacing.WEST),

    FACE_SOUTH_POINT_UP(EnumFacing.SOUTH, EnumFacing.UP),
    FACE_SOUTH_POINT_DOWN(EnumFacing.SOUTH, EnumFacing.DOWN),
    FACE_SOUTH_POINT_EAST(EnumFacing.SOUTH, EnumFacing.EAST),
    FACE_SOUTH_POINT_WEST(EnumFacing.SOUTH, EnumFacing.WEST),

    FACE_WEST_POINT_UP(EnumFacing.WEST, EnumFacing.UP),
    FACE_WEST_POINT_DOWN(EnumFacing.WEST, EnumFacing.DOWN),
    FACE_WEST_POINT_SOUTH(EnumFacing.WEST, EnumFacing.SOUTH),
    FACE_WEST_POINT_NORTH(EnumFacing.WEST, EnumFacing.NORTH),

    FACE_EAST_POINT_UP(EnumFacing.EAST, EnumFacing.UP),
    FACE_EAST_POINT_DOWN(EnumFacing.EAST, EnumFacing.DOWN),
    FACE_EAST_POINT_SOUTH(EnumFacing.EAST, EnumFacing.SOUTH),
    FACE_EAST_POINT_NORTH(EnumFacing.EAST, EnumFacing.NORTH);

    /**
     * This value is what a Dispenser has. It can point in any of the 6 directions.
     */
    public final EnumFacing facing;
    
    /**
     * This is what various RedPower-style machines add. It can only point in 4 directions. It can not point in the facing direction, nor in the opposite direction.
     */
    public final EnumFacing top;
    
    private Orientation[] rotations = new Orientation[EnumFacing.VALUES.length];
    private int rotation;
    private Orientation swapped;
    private EnumFacing[] dirRotations = new EnumFacing[EnumFacing.VALUES.length]; // Admitedly we could just use values() here. But that's ugly.
    
    private static Orientation[] valuesCache = values();

    Orientation(EnumFacing facing, EnumFacing top) {
        this.facing = facing;
        this.top = top;
    }
    
    static {
        for (Orientation o : values()) {
            for (Orientation t : values()) {
                if (o.facing == t.top && o.top == t.facing) {
                    o.swapped = t;
                    break;
                }
            }
        }

        for (Orientation o : values()) {
            o.setupRotationFacing();
        }
        for (Orientation o : values()) {
            o.setupRotationTop();
        }
        for (Orientation o : values()) {
            o.setupRotationAll();
        }
        for (Orientation o : values()) {
            o.setupDirectionRotation();
        }

        if (valuesCache.length == 0) {
            throw new RuntimeException("lolwut");
        }
    }

    private void setupRotationFacing() {
        rotations[facing.getOpposite().ordinal()] = find(facing, SpaceUtils.rotateCounterclockwise(top, facing));
        rotations[facing.ordinal()] = find(facing, SpaceUtils.rotateClockwise(top, facing));
    }

    private void setupRotationTop() {
        rotations[top.getOpposite().ordinal()] = getSwapped().getNextRotationOnFace().getSwapped();
        rotations[top.ordinal()] = getSwapped().getPrevRotationOnFace().getSwapped();
    }

    private void setupRotationAll() {
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (dir.getAxis() != facing.getAxis() && dir.getAxis() != top.getAxis()) {
                if (dir.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                    rotations[dir.ordinal()] = find(facing.rotateAround(dir.getAxis()), top.rotateAround(dir.getAxis()));
                } else {
                    rotations[dir.ordinal()] = find(facing.rotateAround(dir.getAxis()).rotateAround(dir.getAxis()).rotateAround(dir.getAxis()), top.rotateAround(dir.getAxis()).rotateAround(dir.getAxis()).rotateAround(dir.getAxis()));
                }
            }
        }

        int rcount = 0;
        Orientation head = fromDirection(facing);
        for (int i = 0; i < 5; i++) {
            if (head == this) {
                rotation = rcount;
            }
            rcount++;
            head = head.getNextRotationOnFace();
        }
    }

    private void setupDirectionRotation() {
        for (EnumFacing dir : EnumFacing.values()) {
            Vec3d v = SpaceUtils.fromDirection(dir);
            Quaternion.fromOrientation(this).applyRotation(v);
            dirRotations[dir.ordinal()] = SpaceUtils.getClosestDirection(v, null);
        }
    }
    
    private static Orientation find(EnumFacing f, EnumFacing t) {
        for (int i = f.ordinal() * 4; i < f.ordinal() * 4 + 4; i++) {
            Orientation o = valuesCache[i];
            if (o.top == t) {
                return o;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }
    
    public Orientation rotateOnFace(int count) {
        count = count % 4;
        if (count > 0) {
            Orientation here = this;
            while (count > 0) {
                count--;
                here = here.getNextRotationOnFace();
            }
            return here;
        } else if (count < 0) {
            Orientation here = this;
            while (count < 0) {
                count++;
                here = here.getPrevRotationOnFace();
            }
            return here;
        } else {
            return this;
        }
    }
    
    public Orientation getNextRotationOnFace() {
        return rotations[facing.getOpposite().ordinal()];
    }
    
    public Orientation getPrevRotationOnFace() {
        return rotations[facing.ordinal()];
    }
    
    public Orientation getNextRotationOnTop() {
        return rotations[top.getOpposite().ordinal()];
    }
    
    public Orientation getPrevRotationOnTop() {
        return rotations[top.ordinal()];
    }
    
    public Orientation rotateOnTop(int count) {
        return getSwapped().rotateOnFace(count).getSwapped();
    }
    
    public static Orientation getOrientation(int index) {
        if (index >= 0 && index < valuesCache.length) {
            return valuesCache[index];
        }
        return null;
    }
    
    public static Orientation fromDirection(EnumFacing dir) {
        if (dir == null) {
            return null;
        }
        return valuesCache[dir.ordinal()*4];
    }

    @SideOnly(Side.CLIENT)
    private static Matrix4f newMat() {
        Matrix4f ret = new Matrix4f();
        ret.setIdentity();
        return ret;
    }

    @SideOnly(Side.CLIENT)
    public TRSRTransformation toTransformation() {
        Quaternion quat = Quaternion.fromOrientation(this.getSwapped());
        Matrix4f trans = newMat();
        Matrix4f rot = newMat();
        Matrix4f r90 = newMat();

        r90.setRotation(new AxisAngle4f(0, 1, 0, (float) Math.PI / 2));

        trans.setTranslation(new Vector3f(0.5F, 0.5F, 0.5F));
        Matrix4f iTrans = new Matrix4f(trans);
        iTrans.invert();
        rot.setRotation(quat.toJavax());
        rot.mul(r90);

        trans.mul(rot);
        trans.mul(iTrans);

        return new TRSRTransformation(trans);
    }
    
    /**
     * @param newTop
     * @return {@link Orientation} with the same direction, but facing newTop. If the top can't be change to that direction because it is already facing that direction, it returns UNKNOWN.
     */
    public Orientation pointTopTo(EnumFacing newTop) {
        Orientation fzo = this;
        for (int i = 0; i < 4; i++) {
            if (fzo.top == newTop) {
                return fzo;
            }
            fzo = fzo.getNextRotationOnFace();
        }
        return null;
    }

    // TODO: Cache you, cache me!
    public Orientation mirror(@Nonnull Mirror mirror) {
        EnumFacing.Axis axis = null;
        switch (mirror) {
            case NONE:
                return this;
            case FRONT_BACK:
                axis = EnumFacing.Axis.X;
                break;
            case LEFT_RIGHT:
                axis = EnumFacing.Axis.Z;
                break;
        }

        if (facing.getAxis() == axis) {
            return find(facing.getOpposite(), top);
        } else if (top.getAxis() == axis) {
            return find(facing, top.getOpposite());
        } else {
            return this;
        }
    }
    
    public Orientation rotateAround(@Nonnull EnumFacing axis) {
        return rotations[axis.ordinal()];
    }
    
    public int getRotation() {
        return rotation;
    }
    
    public Vec3d getDiagonalVector(Vec3d vec) {
        return new Vec3d(facing.getDirectionVec()).add(new Vec3d(top.getDirectionVec()));
    }

    public Orientation getSwapped() {
        return swapped;
    }

    public EnumFacing applyRotation(EnumFacing dir) {
        return dirRotations[dir.ordinal()];
    }
}
