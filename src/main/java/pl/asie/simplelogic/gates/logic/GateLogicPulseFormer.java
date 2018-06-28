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
import net.minecraft.util.ITickable;
import pl.asie.simplelogic.gates.PartGate;

public class GateLogicPulseFormer extends GateLogic {
	private byte pulse;

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return side == EnumFacing.SOUTH;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		tag = super.writeToNBT(tag, isClient);
		tag.setByte("pl", pulse);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag, boolean isClient) {
		pulse = tag.getByte("pl");
		super.readFromNBT(tag, isClient);
	}

	public void onChanged(PartGate parent) {
		if (pulse == 0) {
			boolean changed = parent.updateInputs(inputValues);
			if (changed) {
				pulse = getInputValueInside(EnumFacing.SOUTH);
				if (pulse != 0) {
					parent.scheduleTick();
				}
				parent.updateInputs(inputValues);
				parent.propagateOutputs();
			}
		}
	}

	@Override
	public boolean tick(PartGate parent) {
		boolean changed = pulse != 0;
		if (changed) {
			super.tick(parent);
			pulse = 0;
			parent.scheduleTick();
			return true;
		} else {
			return super.tick(parent);
		}
	}

	@Override
	public Connection getType(EnumFacing dir) {
		if (dir == EnumFacing.NORTH) {
			return Connection.OUTPUT;
		} else if (dir == EnumFacing.SOUTH) {
			return Connection.INPUT;
		} else {
			return Connection.NONE;
		}
	}

	@Override
	public State getLayerState(int id) {
		boolean hasSignal = getInputValueInside(EnumFacing.SOUTH) != 0;
		switch (id) {
			case 0:
				return State.input(getInputValueInside(EnumFacing.SOUTH));
			case 1:
			case 2:
				return State.bool(!hasSignal);
			case 3:
				return State.bool(hasSignal);
			case 4:
				return State.input(getOutputValueOutside(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputValueInside(EnumFacing.SOUTH)).invert();
			case 1:
				return State.input(getInputValueInside(EnumFacing.SOUTH));
			case 2:
				return State.input(getOutputValueInside(EnumFacing.NORTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		return pulse;
	}
}
