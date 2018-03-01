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

public class GateLogicMultiplexer extends GateLogic {
	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public State getLayerState(int id) {
		boolean isWest = getValueInside(EnumFacing.SOUTH) != 0;
		boolean westOn = getValueInside(EnumFacing.WEST) != 0;
		boolean eastOn = getValueInside(EnumFacing.EAST) != 0;
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.SOUTH));
			case 1:
				return State.input(getValueInside(EnumFacing.WEST));
			case 2:
				return State.input(getValueInside(EnumFacing.EAST));
			case 3:
				return State.bool(isWest && !westOn);
			case 4:
				return State.bool(!isWest && !eastOn);
			case 5:
				return State.input(getValueInside(EnumFacing.SOUTH)).invert();
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		boolean isWest = getValueInside(EnumFacing.SOUTH) != 0;
		boolean westOn = getValueInside(EnumFacing.WEST) != 0;
		boolean eastOn = getValueInside(EnumFacing.EAST) != 0;
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.SOUTH)).invert();
			case 1:
				return (!isWest || westOn) ? State.OFF : State.ON;
			case 2:
				return (isWest || eastOn) ? State.OFF : State.ON;
			case 3:
				return State.input(getValueOutside(EnumFacing.SOUTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		boolean isWest = getValueInside(EnumFacing.SOUTH) != 0;
		return isWest ? getValueInside(EnumFacing.WEST) : getValueInside(EnumFacing.EAST);
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.NORTH ? Connection.OUTPUT_ANALOG :
				(dir == EnumFacing.SOUTH ? Connection.INPUT : Connection.INPUT_ANALOG);
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}
}
