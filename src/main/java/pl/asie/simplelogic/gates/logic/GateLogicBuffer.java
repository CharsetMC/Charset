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

public class GateLogicBuffer extends GateLogic {
	@Override
	public boolean canBlockSide(EnumFacing side) {
		return side == EnumFacing.WEST || side == EnumFacing.EAST;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}

	@Override
	public GateConnection getType(EnumFacing dir) {
		return dir == EnumFacing.SOUTH ? GateConnection.INPUT_ANALOG : GateConnection.OUTPUT;
	}

	@Override
	public GateRenderState getLayerState(int id) {
		switch (id) {
			case 0:
				if (!isSideOpen(EnumFacing.WEST)) {
					return GateRenderState.DISABLED;
				}
				return GateRenderState.input(getOutputValueInside(EnumFacing.WEST));
			case 1:
				if (!isSideOpen(EnumFacing.EAST)) {
					return GateRenderState.DISABLED;
				}
				return GateRenderState.input(getOutputValueInside(EnumFacing.EAST));
			case 2:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH)).invert();
			case 3:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
		}
		return GateRenderState.OFF;
	}

	@Override
	public GateRenderState getTorchState(int id) {
		switch (id) {
			case 0:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
			case 1:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH)).invert();
		}
		return GateRenderState.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		switch (side) {
			case NORTH:
				return getInputValueInside(EnumFacing.SOUTH) > 0 ? 0 : (byte) 15;
			case WEST:
			case EAST:
				if (isSideInverted(EnumFacing.NORTH)) {
					return getInputValueInside(EnumFacing.SOUTH);
				} else {
					return getInputValueInside(EnumFacing.SOUTH) > 0 ? 0 : (byte) 15;
				}
			default:
				return 0;
		}
	}
}
