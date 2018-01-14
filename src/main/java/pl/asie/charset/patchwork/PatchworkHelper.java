/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.patchwork;

import pl.asie.charset.ModCharset;

import java.lang.reflect.Field;

public final class PatchworkHelper {
	private static final Class c;
	private static boolean sentPatchesMsg = false;

	static {
		Class cc;
		try {
			cc = Class.forName("pl.asie.charset.patches.CharsetPatchwork");
		} catch (Exception e) {
			cc = null;
		}
		c = cc;
	}

	private PatchworkHelper() {

	}

	private static void sendPatchesMsgIfNotSent() {
		if (!sentPatchesMsg) {
			sentPatchesMsg = true;
			if (c != null) {
				ModCharset.logger.info("CharsetPatches present!");
			} else {
				ModCharset.logger.info("CharsetPatches not present!");
			}
		}
	}

	public static boolean getBoolean(String name) {
		sendPatchesMsgIfNotSent();

		if (c == null) {
			return false;
		} else {
			try {
				Field f = c.getDeclaredField(name);
				f.setAccessible(true);
				return f.getBoolean(null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
}
