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

public class GateLogicMultiplexer extends GateLogic {
	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public GateRenderState getLayerState(int id) {
		boolean isWest = getInputValueInside(EnumFacing.SOUTH) != 0;
		boolean westOn = getInputValueInside(EnumFacing.WEST) != 0;
		boolean eastOn = getInputValueInside(EnumFacing.EAST) != 0;
		switch (id) {
			case 0:
				return GateRenderState.input(getInputValueInside(EnumFacing.SOUTH));
			case 1:
				return GateRenderState.input(getInputValueInside(EnumFacing.WEST));
			case 2:
				return GateRenderState.input(getInputValueInside(EnumFacing.EAST));
			case 3:
				return GateRenderState.bool(isWest && !westOn);
			case 4:
				return GateRenderState.bool(!isWest && !eastOn);
			case 5:
				return GateRenderState.input(getInputValueInside(EnumFacing.SOUTH)).invert();
		}
		return GateRenderState.OFF;
	}

	@Override
	public GateRenderState getTorchState(int id) {
		boolean isWest = getInputValueInside(EnumFacing.SOUTH) != 0;
		boolean westOn = getInputValueInside(EnumFacing.WEST) != 0;
		boolean eastOn = getInputValueInside(EnumFacing.EAST) != 0;
		switch (id) {
			case 0:
				return GateRenderState.input(getInputValueInside(EnumFacing.SOUTH)).invert();
			case 1:
				return (!isWest || westOn) ? GateRenderState.OFF : GateRenderState.ON;
			case 2:
				return (isWest || eastOn) ? GateRenderState.OFF : GateRenderState.ON;
			case 3:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
		}
		return GateRenderState.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		boolean isWest = getInputValueInside(EnumFacing.SOUTH) != 0;
		return isWest ? getInputValueInside(EnumFacing.WEST) : getInputValueInside(EnumFacing.EAST);
	}

	@Override
	public GateConnection getType(EnumFacing dir) {
		return dir == EnumFacing.NORTH ? GateConnection.OUTPUT_ANALOG :
				(dir == EnumFacing.SOUTH ? GateConnection.INPUT : GateConnection.INPUT_ANALOG);
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}
}
