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

package pl.asie.simplelogic.gates.logic;

public enum GateConnection {
	NONE(false, false, false, false, false),
	INPUT(true, false, true, false, false),
	OUTPUT(false, true, true, false, false),
	INPUT_OUTPUT(true, true, true, false, false),
	INPUT_ANALOG(true, false, true, true, false),
	INPUT_COMPARATOR(true, false, true, true, false),
	INPUT_REPEATER(true, false, false, false, false),
	OUTPUT_ANALOG(false, true, true, true, false),
	INPUT_BUNDLED(true, false, false, false, true),
	OUTPUT_BUNDLED(false, true, false, false, true),
	INPUT_OUTPUT_BUNDLED(true, true, false, false, true);

	private final boolean input, output, redstone, analogRedstone, bundled;

	GateConnection(boolean input, boolean output, boolean redstone, boolean analogRedstone, boolean bundled) {
		this.input = input;
		this.output = output;
		this.redstone = redstone;
		this.analogRedstone = analogRedstone;
		this.bundled = bundled;
	}

	public boolean isComparator() {
		return this == INPUT_COMPARATOR;
	}

	public boolean isInput() {
		return input;
	}

	public boolean isOutput() {
		return output;
	}

	public boolean isRedstone() {
		return redstone;
	}

	public boolean isDigital() {
		return redstone && !analogRedstone;
	}

	public boolean isAnalog() {
		return analogRedstone;
	}

	public boolean isBundled() {
		return bundled;
	}
}
