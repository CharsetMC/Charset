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

import com.google.common.io.ByteArrayDataOutput;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

public class Quaternion {
    public double w, x, y, z;

    //Data functions
    public Quaternion() {
        this(1, 0, 0, 0);
        //NORELEASE.fixme("Should we make this class pure?");
    }
    
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Quaternion(Quaternion orig) {
        this.w = orig.w;
        this.x = orig.x;
        this.y = orig.y;
        this.z = orig.z;
    }
    
    public Quaternion(double[] init) {
        this(init[0], init[1], init[2], init[3]);
        assert init.length == 4;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Quaternion) {
            Quaternion other = (Quaternion) obj;
            return w == other.w && x == other.x && y == other.y && z == other.z;
        }
        return false;
    }
    
    @Override
    public String toString() {
        String m = "";
        double mag = this.magnitude();
        if (mag != 1.0) {
            m = " MAG=" + mag;
        }
        return "Q<w=" + w + ", " + x + ", " + y + ", " + z + ">" + m;
    }
    
    public void writeToTag(NBTTagCompound tag, String prefix) {
        tag.setDouble(prefix+"w", w);
        tag.setDouble(prefix+"x", x);
        tag.setDouble(prefix+"y", y);
        tag.setDouble(prefix+"z", z);
    }
    
    public static Quaternion loadFromTag(NBTTagCompound tag, String prefix) {
        return new Quaternion(tag.getDouble(prefix+"w"), tag.getDouble(prefix+"x"), tag.getDouble(prefix+"y"), tag.getDouble(prefix+"z"));
    }
    
    public void write(ByteArrayDataOutput out) {
        double[] d = toStaticArray();
        for (int i = 0; i < d.length; i++) {
            out.writeDouble(d[i]);
        }
    }
    
    public void write(ByteBuf out) {
        double[] d = toStaticArray();
        for (int i = 0; i < d.length; i++) {
            out.writeDouble(d[i]);
        }
    }
    
    public void write(DataOutputStream out) throws IOException {
        double[] d = toStaticArray();
        for (int i = 0; i < d.length; i++) {
            out.writeDouble(d[i]);
        }
    }
    
    public static Quaternion read(DataInput in) throws IOException {
        double[] d = localStaticArray.get();
        for (int i = 0; i < d.length; i++) {
            d[i] = in.readDouble();
        }
        return new Quaternion(d);
    }
    
    public static Quaternion read(ByteBuf in) throws IOException {
        double[] d = localStaticArray.get();
        for (int i = 0; i < d.length; i++) {
            d[i] = in.readDouble();
        }
        return new Quaternion(d);
    }
    
    /* @Override
    public IDataSerializable serialize(String name_prefix, DataHelper data) throws IOException {
        w = data.asSameShare(name_prefix + "w").putDouble(w);
        x = data.asSameShare(name_prefix + "x").putDouble(x);
        y = data.asSameShare(name_prefix + "y").putDouble(y);
        z = data.asSameShare(name_prefix + "z").putDouble(z);
        return this;
    } */
    
    public double[] fillArray(double[] out) {
        out[0] = w;
        out[1] = x;
        out[2] = y;
        out[3] = z;
        return out;
    }
    
    public double[] toArray() {
        return fillArray(new double[4]);
    }
    
    private static ThreadLocal<double[]> localStaticArray = new ThreadLocal<double[]>() {
        @Override
        protected double[] initialValue() {
            return new double[4];
        };
    };
    
    public double[] toStaticArray() {
        return fillArray(localStaticArray.get());
    }
    
    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }

    public void update(double nw, double nx, double ny, double nz) {
        w = nw;
        x = nx;
        y = ny;
        z = nz;
    }

    public void update(Quaternion other) {
        update(other.w, other.x, other.y, other.z);
    }
    
    public void update(EnumFacing dir) {
        update(w, dir.getDirectionVec().getX(), dir.getDirectionVec().getY(), dir.getDirectionVec().getZ());
    }
    
    public void update(Vec3d v) {
        update(0, v.x, v.y, v.z);
    }

    public Vec3d toVector() {
        return new Vec3d(x, y, z);
    }
    
    /**
     * @return a vector parallel with the imaginary components with length equal to the rotation
     */
    public Vec3d toRotationVector() {
        Vec3d rotVec = toVector().normalize();
        return SpaceUtils.scale(rotVec, getAngleRadians());
    }
    
    public double getAngleRadians() {
        return 2 * Math.acos(w);
    }
    
    //Math functions
    public Quaternion incrNormalize() {
        double normSquared = magnitudeSquared();
        if (normSquared == 1 || normSquared == 0) {
            return this;
        }
        double norm = Math.sqrt(normSquared);
        w /= norm;
        x /= norm;
        y /= norm;
        z /= norm;
        return this;
    }

    /**
     * I think this is broken? Use Quaternion.fromOrientation
     */
    @Deprecated
    public static Quaternion getRotationQuaternion(Orientation orient) {
        return getRotationQuaternionRadians(Math.toRadians(orient.getRotation()*90), orient.facing);
    }
    
    public static Quaternion getRotationQuaternionRadians(double angle, Vec3d axis) {
        double halfAngle = angle/2;
        double sin = Math.sin(halfAngle);
        return new Quaternion(Math.cos(halfAngle), axis.x*sin, axis.y*sin, axis.z*sin);
    }
    
    public static Quaternion getRotationQuaternionRadians(double angle, EnumFacing axis) {
        double halfAngle = angle/2;
        double sin = Math.sin(halfAngle);
        return new Quaternion(Math.cos(halfAngle), axis.getDirectionVec().getX()*sin, axis.getDirectionVec().getY()*sin, axis.getDirectionVec().getZ()*sin);
    }
    
    public static Quaternion getRotationQuaternionRadians(double angle, double ax, double ay, double az) {
        double halfAngle = angle/2;
        double sin = Math.sin(halfAngle);
        return new Quaternion(Math.cos(halfAngle), ax*sin, ay*sin, az*sin);
    }
    
    private static Quaternion[] quat_cache = new Quaternion[25 /*Orientation.values().length recursive reference, bleh*/];
    /***
     * @param orient An {@link Orientation}
     * @return A {@link Quaternion} that should not be mutated.
     */
    public static Quaternion fromOrientation(final Orientation orient) {
        final int ord = orient.ordinal();
        if (quat_cache[ord] != null) {
            return quat_cache[ord];
        }
        if (orient == null) {
            return quat_cache[ord] = new Quaternion();
        }
        final Quaternion q1;
        final double quart = Math.toRadians(90);
        int rotation = orient.getRotation();
        switch (orient.facing) {
        case UP: {
            q1 = Quaternion.getRotationQuaternionRadians(0*quart, EnumFacing.WEST);
            rotation = 5 - rotation;
            break;
        }
        case DOWN: {
            q1 = Quaternion.getRotationQuaternionRadians(2*quart, EnumFacing.WEST);
            rotation = 3 - rotation;
            break;
        }
        case NORTH: {
            q1 = Quaternion.getRotationQuaternionRadians(1*quart, EnumFacing.WEST);
            rotation = 5 - rotation;
            break;
        }
        case SOUTH: {
            q1 = Quaternion.getRotationQuaternionRadians(-1*quart, EnumFacing.WEST);
            rotation = 3 - rotation;
            break;
        }
        case EAST: {
            q1 = Quaternion.getRotationQuaternionRadians(1*quart, EnumFacing.NORTH);
            //rotation = 3 - rotation;
            rotation += Math.abs(orient.top.getDirectionVec().getZ())*2;
            break;
        }
        case WEST: {
            q1 = Quaternion.getRotationQuaternionRadians(-1*quart, EnumFacing.NORTH);
            rotation += Math.abs(orient.top.getDirectionVec().getY())*2;
            break;
        }
        default: return quat_cache[ord] = new Quaternion(); //Won't happen
        }
        final Quaternion q2 = Quaternion.getRotationQuaternionRadians(rotation*quart, orient.facing);
        q2.incrMultiply(q1);
        return quat_cache[ord] = q2;
    }

    @SideOnly(Side.CLIENT)
    public void glRotate() {
        double halfAngle = Math.acos(w);
        double sin = Math.sin(halfAngle);
        GlStateManager.rotate((float) Math.toDegrees(halfAngle*2), (float) (x/sin), (float) (y/sin), (float) (z/sin));
    }
    
    public double dotProduct(Quaternion other) {
        return w*other.w + x*other.x + y*other.y + z*other.z;
    }
    
    public void incrLerp(Quaternion other, double t) {
        other.incrAdd(this, -1);
        other.incrScale(t);
        this.incrAdd(other);
        this.incrNormalize();
    }
    
    public Quaternion lerp(Quaternion other, double t) {
        Quaternion ret = new Quaternion(this);
        ret.incrLerp(other, t);
        return ret;
    }
    
    /**
     * When this Quaternion is going to be interpolated to other, it can be interpolated either the long way around, or the short way.
     * This method makes sure it will be the short interpolation.
     */
    public void incrShortFor(Quaternion other) {
        double cosom = this.dotProduct(other);
        if (cosom < 0) {
            incrScale(-1);
        }
    }
    
    public void incrLongFor(Quaternion other) {
        double cosom = this.dotProduct(other);
        if (cosom > 0) {
            incrScale(-1);
        }
    }
    
    public Quaternion slerp(Quaternion other, double t) {
        if (t == 1) return new Quaternion(other);
        if (t == 0) return new Quaternion(this);
        // from blender/blenlib/intern/math_rotation.c interp_qt_qtqt
        double cosom = this.dotProduct(other);
        // We don't make the dot product > 0, because maybe we'd like long-ways rotation some times
        double omega, sinom, sc1, sc2;

        if ((1.0f - cosom) > 0.0001f) {
            omega = Math.acos(cosom);
            sinom = Math.sin(omega);
            sc1 = Math.sin((1 - t) * omega) / sinom;
            sc2 = Math.sin(t * omega) / sinom;
        } else {
            sc1 = 1.0f - t;
            sc2 = t;
        }
        
        return new Quaternion(
                sc1 * this.w + sc2 * other.w,
                sc1 * this.x + sc2 * other.x,
                sc1 * this.y + sc2 * other.y,
                sc1 * this.z + sc2 * other.z);
    }

    public Quaternion shortSlerp(Quaternion other, double t) {
        // See the other slerp
        double cosom = this.dotProduct(other);
        boolean rev = cosom < 0;
        if (rev) {
            cosom = -cosom;
            other.incrScale(-1);
        }
        double omega, sinom, sc1, sc2;

        if ((1.0f - cosom) > 0.0001f) {
            omega = Math.acos(cosom);
            sinom = Math.sin(omega);
            sc1 = Math.sin((1 - t) * omega) / sinom;
            sc2 = Math.sin(t * omega) / sinom;
        } else {
            sc1 = 1.0f - t;
            sc2 = t;
        }

        Quaternion ret = new Quaternion(
                sc1 * this.w + sc2 * other.w,
                sc1 * this.x + sc2 * other.x,
                sc1 * this.y + sc2 * other.y,
                sc1 * this.z + sc2 * other.z);
        if (rev) other.incrScale(-1);
        return ret;
    }
    
    
    public double getAngleBetween(Quaternion other) {
        double dot = dotProduct(other);
        dot = Math.max(-1, Math.min(1, dot));
        return Math.acos(dot);
    }
    
    /**
     * Also called the norm
     */
    public double magnitude() {
        return Math.sqrt(w*w + x*x + y*y + z*z);
    }
    
    public double magnitudeSquared() {
        return w*w + x*x + y*y + z*z;
    }
    
    public double incrDistance(Quaternion other) {
        incrAdd(other);
        return magnitude();
    }
    
    public Quaternion incrConjugate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }
    
    public Quaternion incrAdd(Quaternion other) {
        w += other.w;
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }
    
    public Quaternion incrAdd(Quaternion other, double scale) {
        w += other.w*scale;
        x += other.x*scale;
        y += other.y*scale;
        z += other.z*scale;
        return this;
    }
    
    public Quaternion incrMultiply(Quaternion other) {
        double nw, nx, ny, nz;
        nw = w*other.w - x*other.x - y*other.y - z*other.z;
        nx = w*other.x + x*other.w + y*other.z - z*other.y;
        ny = w*other.y - x*other.z + y*other.w + z*other.x;
        nz = w*other.z + x*other.y - y*other.x + z*other.w;
        update(nw, nx, ny, nz);
        return this;
    }
    
    /** 
     * Acts like {@link incrMultiply}, but the argument gets incremented instead of this.
     */
    public void incrToOtherMultiply(Quaternion other) {
        double nw, nx, ny, nz;
        nw = w*other.w - x*other.x - y*other.y - z*other.z;
        nx = w*other.x + x*other.w + y*other.z - z*other.y;
        ny = w*other.y - x*other.z + y*other.w + z*other.x;
        nz = w*other.z + x*other.y - y*other.x + z*other.w;
        other.update(nw, nx, ny, nz);
    }
    
    public void incrScale(double scaler) {
        this.w *= scaler;
        this.x *= scaler;
        this.y *= scaler;
        this.z *= scaler;
    }
    
    public void incrUnit() {
        incrScale(1/magnitude());
    }
    
    public void incrReciprocal() {
        double m = magnitude();
        incrConjugate();
        incrScale(1/(m*m));
    }
    
    public void incrCross(Quaternion other) {
        double X = this.y * other.z - this.z * other.y;
        double Y = this.z * other.x - this.x * other.z;
        double Z = this.x * other.y - this.y * other.x;
        this.x = X;
        this.y = Y;
        this.z = Z;
    }
    
    public Quaternion cross(Quaternion other) {
        Quaternion m = new Quaternion(this);
        m.incrCross(other);
        return m;
    }
    
    public void incrRotateBy(Quaternion rotation) {
        rotation.incrToOtherMultiply(this);
        rotation.incrConjugate();
        this.incrMultiply(rotation);
        rotation.incrConjugate();
    }
    
    /**
     * Note: This assumes that this quaternion is normal (magnitude = 1).
     * @param p
     */
    
    private Quaternion _vector_conversion_cache = null;

    public AxisAlignedBB applyRotation(AxisAlignedBB p) {
        Vec3d rotatedOne = applyRotation(new Vec3d(p.minX, p.minY, p.minZ));
        Vec3d rotatedTwo = applyRotation(new Vec3d(p.maxX, p.maxY, p.maxZ));
        return new AxisAlignedBB(rotatedOne, rotatedTwo);
    }

    public Vec3d applyRotation(Vec3d p) {
        //return this * p * this^-1
        if (this.isZero()) {
            return p;
        }
        if (_vector_conversion_cache == null) {
            _vector_conversion_cache = new Quaternion();
        }
        Quaternion point = _vector_conversion_cache;
        point.update(p);
        this.incrToOtherMultiply(point);
        this.incrConjugate();
        point.incrMultiply(this);
        this.incrConjugate();
        return point.toVector();
    }

    public void applyReverseRotation(Vec3d p) {
        incrConjugate();
        applyRotation(p);
        incrConjugate();
    }
    
    //Other math forms
    public double distance(Quaternion other) {
        return add(other).magnitude();
    }
    
    public Quaternion conjugate() {
        Quaternion ret = new Quaternion(this);
        ret.incrConjugate();
        return ret;
    }
    
    public Quaternion add(Quaternion other) {
        Quaternion ret = new Quaternion(this);
        ret.incrAdd(other);
        return ret;
    }
    
    public Quaternion add(Quaternion other, double scale) {
        Quaternion ret = new Quaternion(this);
        ret.incrAdd(other, scale);
        return ret;
    }
    
    public Quaternion multiply(Quaternion other) {
        Quaternion a = new Quaternion(this);
        a.incrMultiply(other);
        return a;
    }
    
    public Quaternion scale(double scaler) {
        Quaternion a = new Quaternion(this);
        a.incrScale(scaler);
        return a;
    }
    
    public Quaternion unit() {
        Quaternion r = new Quaternion(this);
        r.incrUnit();
        return r;
    }
    
    public Quaternion reciprocal() {
        Quaternion r = new Quaternion(this);
        r.incrReciprocal();
        return r;
    }
    
    public Quaternion power(double alpha) {
        // http://en.wikipedia.org/wiki/Quaternion#Exponential.2C_logarithm.2C_and_power
        double norm = this.magnitude();
        double theta = Math.acos(w / norm);
        double qa = Math.pow(norm, alpha);
        double alphaTheta = alpha * theta;
        double W = qa * Math.cos(alpha * theta);
        double sat = Math.sin(alphaTheta);
        return new Quaternion(W, x * sat, y * sat, z * sat);
    }

    public boolean hasNaN() {
        return Double.isNaN(w) || Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }

    public boolean hasInf() {
        return Double.isInfinite(w) || Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z);
    }

    public Quaternion cleanAbnormalNumbers() {
        if (hasNaN() || hasInf()) return new Quaternion();
        return this;
    }

    public Quat4f toJavax() {
        return new Quat4f((float) x, (float) y, (float) z, (float) w);
    }

    public Quat4d toJavaxD() {
        return new Quat4d(x, y, z, w);
    }
}
