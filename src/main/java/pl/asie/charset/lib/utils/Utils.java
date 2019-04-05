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

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class Utils {
	private static final MethodHandle EXPLOSION_SIZE_GETTER;

	static {
		try {
			EXPLOSION_SIZE_GETTER = MethodHandles.lookup().unreflectGetter(ReflectionHelper.findField(Explosion.class, "size", "field_77280_f"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Utils() {

	}

	public static IThreadListener getThreadListener() {
		return UtilProxyCommon.proxy;
	}

	public static float getExplosionSize(Explosion explosion) {
		try {
			return (float) EXPLOSION_SIZE_GETTER.invokeExact(explosion);
		} catch (Throwable t) {
			return 1;
		}
	}

	public static World getLocalWorld(int dim) {
		return UtilProxyCommon.proxy.getLocalWorld(dim);
	}

	public static World getServerWorldOrDefault(World def) {
		return UtilProxyCommon.proxy.getServerWorldOrDefault(def);
	}
}
