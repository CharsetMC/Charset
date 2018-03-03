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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import pl.asie.simplelogic.gates.PartGate;

import java.util.Random;

public class GateLogicRandomizer extends GateLogic {
	private static final Random rand = new Random();

	@Override
	public boolean tick(PartGate parent) {
		byte oldInput = getInputValueInside(EnumFacing.SOUTH);
		parent.updateInputs(inputValues);
		byte newInput = getInputValueInside(EnumFacing.SOUTH);
		if (newInput != oldInput && newInput > 0) {
			// generate random values
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				if (facing != EnumFacing.SOUTH) {
					int r;
					if (newInput <= 8) {
						r = rand.nextInt(16);
					} else {
						r = rand.nextBoolean() ? 15 : 0;
					}
					outputValues[facing.ordinal() - 2] = (byte) r;
				}
			}
			return true;
		} else {
			return newInput != oldInput;
		}
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return side != EnumFacing.SOUTH;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return true;
	}

	@Override
	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.SOUTH ? Connection.INPUT_ANALOG : Connection.OUTPUT_ANALOG;
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputValueInside(EnumFacing.SOUTH));
			case 1:
				return State.input(getOutputValueInside(EnumFacing.WEST));
			case 2:
				return State.input(getOutputValueInside(EnumFacing.NORTH));
			case 3:
				return State.input(getOutputValueInside(EnumFacing.EAST));
		}
		return null;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getOutputValueInside(EnumFacing.WEST));
			case 1:
				return State.input(getOutputValueInside(EnumFacing.NORTH));
			case 2:
				return State.input(getOutputValueInside(EnumFacing.EAST));
		}
		return null;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		return 0;
	}
}
