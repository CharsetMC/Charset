/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

public class GateLogicBundledTransceiver extends GateLogic {
	private int inputState = 0;

	@Override
	public boolean tick(IGateContainer gate) {
		inputState = ((getInputValueInside(EnumFacing.WEST) > 0) ? 2 : 0) | ((getInputValueInside(EnumFacing.EAST) > 0) ? 1 : 0);

		return true;
	}

	@Override
	public void calculateOutputBundled(EnumFacing side, byte[] data) {
		if (inputState == 3) {
			byte[] inputNorth = getInputValueBundled(EnumFacing.NORTH);
			byte[] inputSouth = getInputValueBundled(EnumFacing.SOUTH);
			for (int i = 0; i < 16; i++) {
				data[i] = (byte) Math.max(inputNorth != null ? inputNorth[i] : 0, inputSouth != null ? inputSouth[i] : 0);
			}
			return;
		}

		byte[] src = null;

		switch (side) {
			case SOUTH:
				if (((inputState & 2) != 0)) {
					src = getInputValueBundled(EnumFacing.NORTH);
				}
				break;
			case NORTH:
				if (((inputState & 1) != 0)) {
					src = getInputValueBundled(EnumFacing.SOUTH);
				}
				break;
			default:
				throw new RuntimeException("No bundled input here!");
		}

		if (src == null) {
			for (int i = 0; i < 16; i++) {
				data[i] = 0;
			}
		} else {
			System.arraycopy(src, 0, data, 0, 16);
		}
	}

	@Override
	public GateRenderState getLayerState(int id) {
		switch (id) {
			case 0:
				return GateRenderState.inputOrDisabled(this, EnumFacing.WEST, getInputValueOutside(EnumFacing.WEST));
			case 1:
				return GateRenderState.inputOrDisabled(this, EnumFacing.EAST, getInputValueOutside(EnumFacing.EAST));
			default:
				return GateRenderState.DISABLED;
		}
	}

	@Override
	public GateRenderState getTorchState(int id) {
		switch (id) {
			case 0:
			default:
				return GateRenderState.OFF;
			case 1:
				return GateRenderState.input(getInputValueOutside(EnumFacing.WEST));
			case 2:
				return GateRenderState.input(getInputValueOutside(EnumFacing.EAST));
		}
	}

	@Override
	public GateConnection getType(EnumFacing dir) {
		if (dir.getAxis() == EnumFacing.Axis.Z) {
			return GateConnection.INPUT_OUTPUT_BUNDLED;
		} else {
			return GateConnection.INPUT;
		}
	}
}
