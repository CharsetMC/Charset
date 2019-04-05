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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

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
	public boolean readFromNBT(NBTTagCompound tag, boolean isClient) {
		byte oldPll = pulseLeft;
		byte oldPlr = pulseRight;
		pulseLeft = tag.getByte("pll");
		pulseRight = tag.getByte("plr");
		return super.readFromNBT(tag, isClient) || (oldPll != pulseLeft) || (oldPlr != pulseRight);
	}

	@Override
	public void onChanged(IGateContainer gate) {
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
		if (updateOutputs(gate)) {
			if (pulseRight > 0 && pulseLeft > 0) {
				gate.scheduleRedstoneTick();
			}
			gate.markGateChanged(true);
		} else {
			gate.markGateChanged(false);
		}
	}

	@Override
	public boolean tick(IGateContainer gate) {
		if (pulseLeft != 0 && pulseRight != 0) {
			pulseLeft = pulseRight = 0;
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
			return GateConnection.INPUT_ANALOG;
		}
	}

	@Override
	public GateRenderState getLayerState(int id) {
		switch (id) {
			case 0:
				return GateRenderState.inputOrDisabled(this, EnumFacing.SOUTH, getInputValueInside(EnumFacing.SOUTH));
			case 1:
				return GateRenderState.input(getInputValueInside(EnumFacing.WEST));
			case 2:
				return GateRenderState.input(getInputValueInside(EnumFacing.EAST));
			case 3:
				return GateRenderState.input(pulseLeft);
			case 4:
				return GateRenderState.input(pulseRight);
			case 5:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
		}
		return GateRenderState.OFF;
	}

	@Override
	public GateRenderState getTorchState(int id) {
		switch (id) {
			case 0:
				return GateRenderState.input(pulseLeft);
			case 1:
				return GateRenderState.input(pulseRight);
			case 2:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
		}
		return GateRenderState.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		return pulseLeft > 0 && pulseRight > 0 ? (byte) (Math.max(pulseRight, pulseLeft)) : 0;
	}
}
