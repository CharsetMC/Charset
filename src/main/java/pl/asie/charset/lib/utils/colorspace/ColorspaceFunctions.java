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

package pl.asie.charset.lib.utils.colorspace;

final class ColorspaceFunctions {
	private static final float[] D65_WHITE = {0.9504f, 1.0000f, 1.0888f};
	private static final float E = 0.008856f;
	private static final float K = 903.3f;
	private static final float KE = K*E;
	private static final float E_CBRT = 0.2068930344f;

	public static float[] XYZtoLAB(float[] v) {
		float xr = v[0] / D65_WHITE[0];
		float yr = v[1] / D65_WHITE[1];
		float zr = v[2] / D65_WHITE[2];

		float fx = (xr > E) ? (float) Math.cbrt(xr) : (K*xr + 16)/116.0f;
		float fy = (yr > E) ? (float) Math.cbrt(yr) : (K*yr + 16)/116.0f;
		float fz = (zr > E) ? (float) Math.cbrt(zr) : (K*zr + 16)/116.0f;

		return new float[] {
				116*fy - 16,
				500*(fx - fy),
				200*(fy - fz)
		};
	}

	public static float[] LABtoXYZ(float[] v) {
		float fy = (v[0] + 16)/116.0f;
		float fx = v[1]/500.0f + fy;
		float fz = fy - v[2]/200.0f;

		float yr;
		float xr = (fx > E_CBRT) ? (fx*fx*fx) : (116*fx - 16)/K;
		float zr = (fz > E_CBRT) ? (fz*fz*fz) : (116*fz - 16)/K;
		if (v[0] > KE) {
			yr = ((v[0]+16)/116.0f);
			yr *= yr * yr;
		} else {
			yr = v[0]/K;
		}

		return new float[] {
				xr * D65_WHITE[0],
				yr * D65_WHITE[1],
				zr * D65_WHITE[2]
		};
	}

	public static float[] sRGBtoXYZ(float[] v) {
		return new float[] {
				(float) (0.4124564*v[0] + 0.3575761*v[1] + 0.1804375*v[2]),
				(float) (0.2126729*v[0] + 0.7151522*v[1] + 0.0721750*v[2]),
				(float) (0.0193339*v[0] + 0.1191920*v[1] + 0.9503041*v[2])
		};
	}

	public static float[] XYZtosRGB(float[] v) {
		return new float[] {
				(float) (3.2404542*v[0] + -1.5371385*v[1] + -0.4985314*v[2]),
				(float) (-0.9692660*v[0] + 1.8760108*v[1] + 0.0415560*v[2]),
				(float) (0.0556434*v[0] + -0.2040259*v[1] + 1.0572252*v[2])
		};
	}

	public static float[] sRGBtoYUV(float[] v) {
		return new float[] {
			0.299f * v[0] + 0.587f * v[1] + 0.114f * v[2],
			-0.147f * v[0] - 0.289f * v[1] + 0.436f * v[2],
			0.615f * v[0] - 0.515f * v[1] - 0.100f * v[2]
		};
	}

	public static float[] sRGBtoYIQ(float[] v) {
		return new float[] {
			0.299f * v[0] + 0.587f * v[1] + 0.114f * v[2],
			0.596f * v[0] - 0.274f * v[1] - 0.322f * v[2],
			0.211f * v[0] - 0.523f * v[1] + 0.312f * v[2]
		};
	}

	public static float[] sRGBtoWeightedRGB(float[] v) {
		return new float[] {
				(float) Math.sqrt(2 + (v[0] / 255)) * v[0],
				2 * v[1],
				(float) Math.sqrt(3 - (v[0] / 255)) * v[2]
		};
	}
}
