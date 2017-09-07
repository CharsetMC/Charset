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

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class MethodHandleHelper {
    private MethodHandleHelper() {

    }

    public static MethodHandle findMethod(Class c, String nameDeobf, String nameObf, Class<?>... types) {
        try {
            return MethodHandles.lookup().unreflect(
                    ReflectionHelper.findMethod(c, nameDeobf, nameObf, types)
            );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldGetter(Class c, String... names) {
        try {
            return MethodHandles.lookup().unreflectGetter(ReflectionHelper.findField(c, names));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldSetter(Class c, String... names) {
        try {
            return MethodHandles.lookup().unreflectSetter(ReflectionHelper.findField(c, names));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldGetter(String s, String... names) {
        try {
            return findFieldGetter(Class.forName(s), names);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldSetter(String s, String... names) {
        try {
            return findFieldSetter(Class.forName(s), names);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
