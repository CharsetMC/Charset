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

package pl.asie.charset.lib.utils;

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
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class Quaternion {
    public static final Quaternion ORIGIN = new Quaternion();
    public final double w, x, y, z;

    //Data functions
    public Quaternion() {
        this(1, 0, 0, 0);
    }
    
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Quaternion(Quaternion orig) {
        this(orig.w, orig.x, orig.y, orig.z);
    }
    
    public Quaternion(double[] init) {
        this(init[0], init[1], init[2], init[3]);
        assert init.length == 4;
    }

    public static Quaternion fromDirection(double w, EnumFacing dir) {
        return new Quaternion(w, dir.getDirectionVec().getX(), dir.getDirectionVec().getY(), dir.getDirectionVec().getZ());
    }

    public Quaternion(Vec3d v) {
        this(0, v.x, v.y, v.z);
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
    
    public void write(DataOutput out) throws IOException {
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

    private static ThreadLocal<double[]> localStaticArray = ThreadLocal.withInitial(() -> new double[4]);

    public double[] toStaticArray() {
        return fillArray(localStaticArray.get());
    }
    
    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
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
    public Quaternion normalize() {
        double normSquared = magnitudeSquared();
        if (normSquared == 1 || normSquared == 0) {
            return this;
        }
        double norm = Math.sqrt(normSquared);
        return new Quaternion(w / norm, x / norm, y / norm, z / norm);
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
            return quat_cache[ord] = ORIGIN;
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
        default: return quat_cache[ord] = ORIGIN; //Won't happen
        }
        final Quaternion q2 = Quaternion.getRotationQuaternionRadians(rotation*quart, orient.facing);
        return quat_cache[ord] = q2.multiply(q1);
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
    
    public Quaternion lerp(Quaternion other, double t) {
        return this.add(other.add(this, -1).scale(t)).normalize();
    }

    /**
     * When this Quaternion is going to be interpolated to other, it can be interpolated either the long way around, or the short way.
     * This method makes sure it will be the short interpolation.
     */
    public Quaternion shortFor(Quaternion other) {
        double cosom = this.dotProduct(other);
        if (cosom < 0) {
            return scale(-1);
        } else {
            return this;
        }
    }
    
    public Quaternion longFor(Quaternion other) {
        double cosom = this.dotProduct(other);
        if (cosom > 0) {
            return scale(-1);
        } else {
            return this;
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
            other = other.scale(-1);
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
    
    public Quaternion conjugate() {
        return new Quaternion(w, -x, -y, -z);
    }
    
    public Quaternion add(Quaternion other) {
        return new Quaternion(w + other.w, x + other.x, y + other.y, z + other.z);
    }
    
    public Quaternion add(Quaternion other, double scale) {
        return new Quaternion(w + other.w * scale, x + other.x * scale, y + other.y * scale, z + other.z * scale);
    }
    
    public Quaternion multiply(Quaternion other) {
        double nw, nx, ny, nz;
        nw = w*other.w - x*other.x - y*other.y - z*other.z;
        nx = w*other.x + x*other.w + y*other.z - z*other.y;
        ny = w*other.y - x*other.z + y*other.w + z*other.x;
        nz = w*other.z + x*other.y - y*other.x + z*other.w;
        return new Quaternion(nw, nx, ny, nz);
    }
    
    public Quaternion scale(double scaler) {
        return new Quaternion(w * scaler, x * scaler, y * scaler, z * scaler);
    }
    
    public Quaternion unit() {
        return scale(1/magnitude());
    }
    
    public Quaternion reciprocal() {
        double m = magnitude();
        return this.conjugate().scale(1/(m*m));
    }
    
    public Quaternion cross(Quaternion other) {
        double X = this.y * other.z - this.z * other.y;
        double Y = this.z * other.x - this.x * other.z;
        double Z = this.x * other.y - this.y * other.x;
        return new Quaternion(w, X, Y, Z);
    }

    public Quaternion rotateBy(Quaternion rotation) {
        return this.multiply(rotation).multiply(rotation.conjugate());
    }
    
    /**
     * Note: This assumes that this quaternion is normal (magnitude = 1).
     * @param p
     */

    public AxisAlignedBB applyRotation(AxisAlignedBB p) {
        Vec3d rotatedOne = applyRotation(new Vec3d(p.minX, p.minY, p.minZ));
        Vec3d rotatedTwo = applyRotation(new Vec3d(p.maxX, p.maxY, p.maxZ));
        return SpaceUtils.from(rotatedOne, rotatedTwo);
    }

    public Vec3d applyRotation(Vec3d p) {
        //return this * p * this^-1
        if (this.isZero()) {
            return p;
        } else {
            return this.multiply(new Quaternion(p)).multiply(this.conjugate()).toVector();
        }
    }

    public Vec3d applyReverseRotation(Vec3d p) {
        return conjugate().applyRotation(p);
    }
    
    //Other math forms
    public double distance(Quaternion other) {
        return add(other).magnitude();
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
        if (hasNaN() || hasInf()) return ORIGIN;
        return this;
    }

    public Quat4f toJavax() {
        return new Quat4f((float) x, (float) y, (float) z, (float) w);
    }

    public Quat4d toJavaxD() {
        return new Quat4d(x, y, z, w);
    }
}
