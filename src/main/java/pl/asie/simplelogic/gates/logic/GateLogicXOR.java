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
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public GateConnection getType(EnumFacing dir) {
		if (dir == EnumFacing.SOUTH) {
			return GateConnection.NONE;
		} else {
			return dir == EnumFacing.NORTH ? GateConnection.OUTPUT : GateConnection.INPUT;
		}
	}

	@Override
	public GateRenderState getLayerState(int id) {
		switch (id) {
			case 0:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
			case 1:
				return GateRenderState.input(getInputValueInside(EnumFacing.WEST));
			case 2:
				return GateRenderState.input(getInputValueInside(EnumFacing.EAST));
			case 3:
				return GateRenderState.bool(getInputValueInside(EnumFacing.WEST) == 0 && getInputValueInside(EnumFacing.EAST) == 0);
			case 4:
				return GateRenderState.input(getOutputValueOutside(EnumFacing.NORTH));
		}
		return GateRenderState.OFF;
	}

	@Override
	public GateRenderState getTorchState(int id) {
		switch (id) {
			case 0:
				return GateRenderState.input(getInputValueInside(EnumFacing.WEST)).invert();
			case 1:
				return GateRenderState.input(getInputValueInside(EnumFacing.EAST)).invert();
			case 2:
				return GateRenderState.bool(getInputValueInside(EnumFacing.WEST) == 0 && getInputValueInside(EnumFacing.EAST) == 0);
		}
		return GateRenderState.ON;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		if (facing == EnumFacing.NORTH) {
			return digiToRs(rsToDigi(getInputValueInside(EnumFacing.WEST)) ^ rsToDigi(getInputValueInside(EnumFacing.EAST)));
		} else {
			return 0;
		}
	}

	private boolean rsToDigi(byte v) {
		return v > 0;
	}

	private byte digiToRs(boolean v) {
		return v ? (byte) 15 : 0;
	}
}
