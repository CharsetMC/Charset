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

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class MathUtils {
    private MathUtils() {

    }

    public static float interpolate(float a, float b, float amount) {
        return (a * (1.0f - amount) + b * amount);
    }

    public static float linePointDistance(Vec3d lineStart, Vec3d lineEnd, Vec3d point) {
        Vec3d first = lineStart.subtract(point);
        Vec3d second = lineEnd.subtract(point);

        return MathHelper.sqrt(first.crossProduct(second).lengthSquared() / second.lengthSquared());
    }
}
