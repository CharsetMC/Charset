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

import net.minecraft.util.EnumFacing;

public enum GateRenderState {
	NO_RENDER,
	OFF,
	ON,
	DISABLED;

	public GateRenderState invert() {
		switch (this) {
			case OFF:
				return ON;
			case ON:
				return OFF;
			default:
				return this;
		}
	}

	public static GateRenderState inputOrDisabled(GateLogic logic, EnumFacing facing, byte v) {
		if (!logic.isSideOpen(facing)) {
			return GateRenderState.DISABLED;
		} else {
			return input(v);
		}
	}

	public static GateRenderState input(byte i) {
		return i > 0 ? ON : OFF;
	}

	public static GateRenderState bool(boolean v) {
		return v ? ON : OFF;
	}
}
