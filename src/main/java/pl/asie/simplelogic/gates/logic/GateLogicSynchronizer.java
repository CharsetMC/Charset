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

public class GateLogicSynchronizer extends GateLogic {
	private byte pulseLeft, pulseRight;

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return side == EnumFacing.SOUTH;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return side == EnumFacing.NORTH;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		tag = super.writeToNBT(tag, isClient);
		tag.setByte("pll", pulseLeft);
		tag.setByte("plr", pulseRight);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag, boolean isClient) {
		pulseLeft = tag.getByte("pll");
		pulseRight = tag.getByte("plr");
		super.readFromNBT(tag, isClient);
	}

	public void onChanged(PartGate gate) {
		boolean changed = gate.updateInputs(inputValues);
		if (changed) {
			if (getInputValueInside(EnumFacing.SOUTH) > 0) {
				pulseLeft = pulseRight = 0;
			} else {
				byte newPulseLeft = getInputValueInside(EnumFacing.WEST);
				byte newPulseRight = getInputValueInside(EnumFacing.EAST);
				if (newPulseLeft > pulseLeft) {
					pulseLeft = newPulseLeft;
				}
				if (newPulseRight > pulseRight) {
					pulseRight = newPulseRight;
				}
			}
			if (pulseRight > 0 && pulseLeft > 0) {
				gate.scheduleTick();
				updateOutputs();
			}
			gate.propagateOutputs();
		}
	}

	@Override
	public boolean tick(PartGate gate) {
		if (pulseLeft > 0 && pulseRight > 0) {
			pulseLeft = pulseRight = 0;
		}
		return super.tick(gate);
	}

	@Override
	public Connection getType(EnumFacing dir) {
		if (dir == EnumFacing.NORTH) {
			return Connection.OUTPUT;
		} else if (dir == EnumFacing.SOUTH) {
			return Connection.INPUT;
		} else {
			return Connection.INPUT_ANALOG;
		}
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputValueInside(EnumFacing.SOUTH));
			case 1:
				return State.input(getInputValueInside(EnumFacing.WEST));
			case 2:
				return State.input(getInputValueInside(EnumFacing.EAST));
			case 3:
				return State.input(pulseLeft);
			case 4:
				return State.input(pulseRight);
			case 5:
				return State.input(getOutputValueInside(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(pulseLeft);
			case 1:
				return State.input(pulseRight);
			case 2:
				return State.input(getOutputValueInside(EnumFacing.NORTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		return pulseLeft > 0 && pulseRight > 0 ? (byte) (Math.max(pulseRight, pulseLeft)) : 0;
	}
}
