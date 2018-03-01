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

package pl.asie.simplelogic.gates.logic;

import net.minecraft.util.EnumFacing;

public class GateLogicNOR extends GateLogic {
	@Override
	public State getLayerState(int id) {
	switch (id) {
		case 0:
			return State.input(getValueOutside(EnumFacing.NORTH));
		case 1:
			if (!isSideOpen(EnumFacing.WEST)) {
				return State.DISABLED;
			}
			return State.input(getValueInside(EnumFacing.WEST));
		case 2:
			if (!isSideOpen(EnumFacing.EAST)) {
				return State.DISABLED;
			}
			return State.input(getValueInside(EnumFacing.EAST));
		case 3:
			if (!isSideOpen(EnumFacing.SOUTH)) {
				return State.DISABLED;
			}
			return State.input(getValueInside(EnumFacing.SOUTH));
		case 4:
			return State.input(getValueInside(EnumFacing.NORTH));
	}
	return State.OFF;
}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.NORTH)).invert();
			case 1:
				return State.input(getValueInside(EnumFacing.NORTH));
		}
		return State.ON;
	}

	@Override
	public byte calculateOutputInside(EnumFacing side) {
		if (side == EnumFacing.NORTH) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				if (isSideOpen(facing) && facing != EnumFacing.NORTH) {
					if (getValueInside(facing) != 0) {
						return 0;
					}
				}
			}
		}
		return 15;
	}
}
