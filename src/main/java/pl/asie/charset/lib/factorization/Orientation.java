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

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.AxisAngle4f;

public enum Orientation {
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
    
    private Orientation nextFaceRotation, prevFaceRotation;
    private int rotation;
    private Orientation swapped;
    private EnumFacing[] dirRotations = new EnumFacing[EnumFacing.values().length]; // Admitedly we could just use values() here. But that's ugly.
    
    private static Orientation[] valuesCache = values();
    
    Orientation(EnumFacing facing, EnumFacing top) {
        this.facing = facing;
        this.top = top;
    }
    
    static {
        for (Orientation o : values()) {
            o.setup();
        }
        for (Orientation o : values()) {
            o.setupRotation();
        }
        for (Orientation o : values()) {
            for (Orientation t : values()) {
                if (o.facing == t.top && o.top == t.facing) {
                    o.swapped = t;
                    break;
                }
            }
        }
        for (Orientation o : values()) {
            o.setupDirectionRotation();
        }
        if (valuesCache.length == 0) {
            throw new RuntimeException("lolwut");
        }
    }

    private void setup() {
        nextFaceRotation = find(facing, SpaceUtil.rotate(top, facing));
        prevFaceRotation = find(facing, SpaceUtil.rotateBack(top, facing));
    }
    
    private void setupRotation() {
        int rcount = 0;
        Orientation head = fromDirection(facing);
        for (int i = 0; i < 5; i++) {
            if (head == this) {
                rotation = rcount;
            }
            rcount++;
            head = head.nextFaceRotation;
        }
    }

    private void setupDirectionRotation() {
        for (EnumFacing dir : EnumFacing.values()) {
            Vec3d v = SpaceUtil.fromDirection(dir);
            Quaternion.fromOrientation(this).applyRotation(v);
            dirRotations[dir.ordinal()] = SpaceUtil.round(v, null);
        }
    }
    
    private static Orientation find(EnumFacing f, EnumFacing t) {
        for (Orientation o : values()) {
            if (o.facing == f && o.top == t) {
                return o;
            }
        }
        return null;
    }

    
    public Orientation rotateOnFace(int count) {
        count = count % 4;
        if (count > 0) {
            Orientation here = this;
            while (count > 0) {
                count--;
                here = here.nextFaceRotation;
            }
            return here;
        } else if (count < 0) {
            Orientation here = this;
            while (count < 0) {
                count++;
                here = here.prevFaceRotation;
            }
            return here;
        } else {
            return this;
        }
    }
    
    public Orientation getNextRotationOnFace() {
        return nextFaceRotation;
    }
    
    public Orientation getPrevRotationOnFace() {
        return prevFaceRotation;
    }
    
    public Orientation getNextRotationOnTop() {
        return getSwapped().getNextRotationOnFace().getSwapped();
    }
    
    public Orientation getPrevRotationOnTop() {
        return getSwapped().getPrevRotationOnFace().getSwapped();
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
    private static javax.vecmath.Matrix4f newMat() {
        javax.vecmath.Matrix4f ret = new javax.vecmath.Matrix4f();
        ret.setIdentity();
        return ret;
    }

    @SideOnly(Side.CLIENT)
    public TRSRTransformation toTransformation() {
        Quaternion fzq = Quaternion.fromOrientation(this.getSwapped());
        javax.vecmath.Matrix4f trans = newMat();
        javax.vecmath.Matrix4f rot = newMat();
        javax.vecmath.Matrix4f r90 = newMat();

        r90.setRotation(new AxisAngle4f(0, 1, 0, (float) Math.PI / 2));

        trans.setTranslation(new javax.vecmath.Vector3f(0.5F, 0.5F, 0.5F));
        javax.vecmath.Matrix4f iTrans = new javax.vecmath.Matrix4f(trans);
        iTrans.invert();
        rot.setRotation(fzq.toJavax());
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
            fzo = fzo.nextFaceRotation;
        }
        return null;
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
