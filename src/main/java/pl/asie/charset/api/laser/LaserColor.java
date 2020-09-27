/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.charset.api.laser;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum LaserColor implements IStringSerializable {
	NONE,
	BLUE,
	GREEN,
	CYAN,
	RED,
	MAGENTA,
	YELLOW,
	WHITE;

	public static final LaserColor[] VALUES = values();
	public final boolean red, green, blue;
	private final String nameLowercase;

	LaserColor() {
		blue = (this.ordinal() & 1) != 0;
		green = (this.ordinal() & 2) != 0;
		red = (this.ordinal() & 4) != 0;
		nameLowercase = name().toLowerCase(Locale.ROOT);
	}

	public LaserColor union(LaserColor other) {
		return VALUES[this.ordinal() | other.ordinal()];
	}

	public LaserColor intersection(LaserColor other) {
		return VALUES[this.ordinal() & other.ordinal()];
	}

	public LaserColor difference(LaserColor other) {
		return VALUES[this.ordinal() & (~other.ordinal())];
	}

	public LaserColor inversion() {
		return VALUES[this.ordinal() ^ 7];
	}

	@Override
	public String getName() {
		return nameLowercase;
	}
}
