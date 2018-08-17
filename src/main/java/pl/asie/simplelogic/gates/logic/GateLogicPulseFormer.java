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

// TODO: add burnout
public class GateLogicPulseFormer extends GateLogic {
	private long checkTime = -1;
	private long tickTime = -1;
	private byte prevValue;
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
	public boolean readFromNBT(NBTTagCompound tag, boolean isClient) {
		byte oldPl = pulse;
		pulse = tag.getByte("pl");
		return super.readFromNBT(tag, isClient) || (oldPl != pulse);
	}

	@Override
	public boolean updateInputs(IGateContainer gate) {
		long time = gate.getGateWorld().getTotalWorldTime();
		if (checkTime != time) {
			checkTime = time;
			prevValue = getInputValueInside(EnumFacing.SOUTH);
		}

		return super.updateInputs(gate);
	}


	@Override
	public void onChanged(IGateContainer gate) {
		long time = gate.getGateWorld().getTotalWorldTime();
		if (pulse == 0 || tickTime == time) {
			pulse = getInputValueInside(EnumFacing.SOUTH);
			if (pulse == prevValue) {
				pulse = 0;
			}
			tickTime = time;
			if (updateOutputs(gate)) {
				if (pulse != 0) {
					gate.scheduleRedstoneTick();
				}
				gate.markGateChanged(true);
			}
		}
	}

	@Override
	public boolean tick(IGateContainer gate) {
		if (pulse != 0) {
			pulse = 0;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public GateConnection getType(EnumFacing dir) {
		if (dir == EnumFacing.NORTH) {
			return GateConnection.OUTPUT;
		} else if (dir == EnumFacing.SOUTH) {
			return GateConnection.INPUT;
		} else {
			return GateConnection.NONE;
		}
	}

	@Override
	public GateRenderState getLayerState(int id) {
		boolean hasSignal = getInputValueInside(EnumFacing.SOUTH) != 0;
		switch (id) {
			case 0:
				return GateRenderState.input(getInputValueInside(EnumFacing.SOUTH));
			case 1:
			case 2:
				return GateRenderState.bool(!hasSignal);
			case 3:
				return GateRenderState.bool(hasSignal);
			case 4:
				return GateRenderState.input(getOutputValueOutside(EnumFacing.NORTH));
		}
		return GateRenderState.OFF;
	}

	@Override
	public GateRenderState getTorchState(int id) {
		switch (id) {
			case 0:
				return GateRenderState.input(getInputValueInside(EnumFacing.SOUTH)).invert();
			case 1:
				return GateRenderState.input(getInputValueInside(EnumFacing.SOUTH));
			case 2:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
		}
		return GateRenderState.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		return pulse;
	}
}
