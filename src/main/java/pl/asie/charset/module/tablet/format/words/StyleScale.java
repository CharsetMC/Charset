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

package pl.asie.charset.module.tablet.format.words;

import pl.asie.charset.module.tablet.format.api.IStyle;

import java.util.List;

public class StyleScale implements IStyle {
	public final float scale;

	public StyleScale(float scale) {
		this.scale = scale;
	}

	public static float get(List<IStyle> styleList) {
		float scale = 1.0f;

		for (IStyle style : styleList) {
			if (style instanceof StyleScale) {
				scale *= ((StyleScale) style).scale;
			}
		}

		return scale;
	}
}
