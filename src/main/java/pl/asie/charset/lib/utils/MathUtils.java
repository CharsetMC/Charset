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

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public final class MathUtils {
    private MathUtils() {

    }

    public static Matrix4f newJavaxIdentityMat() {
        Matrix4f ret = new Matrix4f();
        ret.setIdentity();
        return ret;
    }

    public static float interpolate(float a, float b, float amount) {
        return (a * (1.0f - amount) + b * amount);
    }

    public static float linePointDistance(Vec3d lineStart, Vec3d lineEnd, Vec3d point) {
        Vec3d first = lineStart.subtract(point);
        Vec3d second = lineEnd.subtract(point);

        return MathHelper.sqrt(first.crossProduct(second).lengthSquared() / second.lengthSquared());
    }

    public static Vec3d interpolate(Vec3d one, Vec3d two, float amount) {
        return new Vec3d(
                one.x + (two.x - one.x) * amount,
                one.y + (two.y - one.y) * amount,
                one.z + (two.z - one.z) * amount
        );
    }
}
