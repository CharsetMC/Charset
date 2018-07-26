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

public class GateLogicBundledInverter extends GateLogic {
	private byte[] output = new byte[16];

	@Override
	public boolean tick(PartGate gate) {
		boolean bundledInputChange = false;

		byte[] input = gate.getBundledInput(EnumFacing.SOUTH);
		for (int i = 0; i < 16; i++) {
			byte v = (byte) ((input[i]) ^ 0xFF);
			if (v != output[i]) {
				bundledInputChange = true;
				output[i] = v;
			}
		}

		return bundledInputChange;
	}

	@Override
	public byte[] getOutputValueBundled(EnumFacing side) {
		return output;
	}c

	@Override
	public State getLayerState(int id) {
		return State.DISABLED;
	}

	@Override
	public State getTorchState(int id) {
		return State.OFF;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		return 0;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		if (dir == EnumFacing.SOUTH) {
			return Connection.INPUT_BUNDLED;
		} else {
			return Connection.OUTPUT_BUNDLED;
		}
	}
}
