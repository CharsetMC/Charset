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
import pl.asie.simplelogic.gates.PartGate;

import java.util.Arrays;

public class GateLogicBundledTransceiver extends GateLogic {
	private byte[] inputNorth = new byte[16], inputSouth = new byte[16];
	private byte[] inputCombined = new byte[16];
	private int inputState = 0;

	@Override
	public boolean tick(IGateContainer gate) {
		boolean inputChange = gate.updateRedstoneInputs(inputValues);
		boolean bundledInputChange = false;

		if (inputChange) {
			inputState = ((getInputValueInside(EnumFacing.WEST) > 0) ? 2 : 0) | ((getInputValueInside(EnumFacing.EAST) > 0) ? 1 : 0);
		}

		byte[] newInputNorth = gate.getBundledInput(EnumFacing.NORTH);
		byte[] newInputSouth = gate.getBundledInput(EnumFacing.SOUTH);

		if (!Arrays.equals(inputNorth, newInputNorth)) {
			inputNorth = newInputNorth;
			bundledInputChange = true;
		}

		if (!Arrays.equals(inputSouth, newInputSouth)) {
			inputSouth = newInputSouth;
			bundledInputChange = true;
		}

		if (bundledInputChange && inputState == 3) {
			for (int i = 0; i < 16; i++) {
				inputCombined[i] = (byte) Math.max(inputNorth[i], inputSouth[i]);
			}
		}

		return inputChange || bundledInputChange;
	}

	@Override
	public byte[] getOutputValueBundled(EnumFacing side) {
		if (inputState == 3) {
			return inputCombined;
		}

		switch (side) {
			case SOUTH:
				return ((inputState & 2) != 0) ? inputNorth : new byte[16];
			case NORTH:
				return ((inputState & 1) != 0) ? inputSouth : new byte[16];
			default:
				throw new RuntimeException("No bundled input here!");
		}
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputValueOutside(EnumFacing.WEST));
			case 1:
				return State.input(getInputValueOutside(EnumFacing.EAST));
			default:
				return State.DISABLED;
		}
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
			default:
				return State.OFF;
			case 1:
				return State.input(getInputValueOutside(EnumFacing.WEST));
			case 2:
				return State.input(getInputValueOutside(EnumFacing.EAST));
		}
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		return 0;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		if (dir.getAxis() == EnumFacing.Axis.Z) {
			return Connection.INPUT_OUTPUT_BUNDLED;
		} else {
			return Connection.INPUT;
		}
	}
}
