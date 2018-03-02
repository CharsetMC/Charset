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

public class GateLogicXOR extends GateLogic {
	@Override
	public Connection getType(EnumFacing dir) {
		if (dir == EnumFacing.SOUTH) {
			return Connection.NONE;
		} else {
			return dir == EnumFacing.NORTH ? Connection.OUTPUT : Connection.INPUT;
		}
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getOutputValueInside(EnumFacing.NORTH));
			case 1:
				return State.input(getInputValueInside(EnumFacing.WEST));
			case 2:
				return State.input(getInputValueInside(EnumFacing.EAST));
			case 3:
				return State.bool(getInputValueInside(EnumFacing.WEST) == 0 && getInputValueInside(EnumFacing.EAST) == 0);
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputValueInside(EnumFacing.WEST)).invert();
			case 1:
				return State.input(getInputValueInside(EnumFacing.EAST)).invert();
			case 2:
				return State.bool(getInputValueInside(EnumFacing.WEST) == 0 && getInputValueInside(EnumFacing.EAST) == 0).invert();
		}
		return State.ON;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		if (facing == EnumFacing.NORTH) {
			return digiToRs(rsToDigi(getInputValueInside(EnumFacing.WEST)) ^ rsToDigi(getInputValueInside(EnumFacing.EAST)));
		} else {
			return 0;
		}
	}
}
