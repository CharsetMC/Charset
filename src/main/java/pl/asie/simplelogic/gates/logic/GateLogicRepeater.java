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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.component.NotificationComponentString;
import pl.asie.simplelogic.gates.PartGate;

// TODO: Add locking
public class GateLogicRepeater extends GateLogic {
	private static final int[] signalValues = {
		1, 2, 3, 4
	};

	private byte ticks = 0;
	private byte repeatedSignal;
	private byte valueMode = 0;

	public Connection getType(EnumFacing dir) {
		switch (dir) {
			case NORTH:
				return Connection.OUTPUT;
			case SOUTH:
				return Connection.INPUT;
			case WEST:
			case EAST:
				return Connection.INPUT_REPEATER;
			default:
				return Connection.NONE;
		}
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return false;
	}

	@Override
	public boolean onRightClick(PartGate gate, EntityPlayer playerIn, Vec3d vec, EnumHand hand) {
		if (!playerIn.isSneaking()) {
			valueMode = (byte) ((valueMode + 1) % signalValues.length);
			gate.markBlockForUpdate();
		}

		new Notice(gate, NotificationComponentString.translated("notice.simplelogic.gate.repeater.ticks", NotificationComponentString.raw(Integer.toString(signalValues[valueMode]))))
				.sendTo(playerIn);
		return true;
	}

	public boolean isRepeaterLocked() {
		return getInputValueInside(EnumFacing.WEST) > 0 || getInputValueInside(EnumFacing.EAST) > 0;
	}

	@Override
	public boolean shouldTick() {
		return !isRepeaterLocked();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		super.writeToNBT(tag, isClient);
		tag.setByte("rtk", ticks);
		tag.setByte("rrs", repeatedSignal);
		tag.setByte("rvm", valueMode);
		return tag;
	}

	@Override
	public boolean readFromNBT(NBTTagCompound tag, boolean isClient) {
		byte oldTicks = ticks;
		byte oldRepeatedSignal = repeatedSignal;
		byte oldValueMode = valueMode;
		ticks = tag.getByte("rtk");
		repeatedSignal = tag.getByte("rrs");
		valueMode = tag.getByte("rvm");
		return super.readFromNBT(tag, isClient) || (oldRepeatedSignal != repeatedSignal) || (oldValueMode != valueMode);
	}

	@Override
	public State getLayerState(int id) {
		return State.input(repeatedSignal);
	}

	@Override
	public State getTorchState(int id) {
		if (id == 0) {
			return State.input(repeatedSignal);
		} else if (id == valueMode+1) {
			return isRepeaterLocked() ? State.DISABLED : State.input(repeatedSignal);
		} else {
			return State.NO_RENDER;
		}
	}

	private byte getInputSignal() {
		return getInputValueInside(EnumFacing.SOUTH);
	}

	@Override
	public void onChanged(PartGate gate) {
		System.out.println("ch " + gate.getPos());

		boolean changed = gate.updateRedstoneInput(inputValues, EnumFacing.WEST) | gate.updateRedstoneInput(inputValues, EnumFacing.EAST);
		if (changed) {
			gate.markBlockForUpdate();
			System.out.println("ch2 " + gate.getPos());
		}

		if (isRepeaterLocked() || (repeatedSignal != 0 && ticks < 2)) {
			return;
		}

		changed |= gate.updateRedstoneInput(inputValues, EnumFacing.SOUTH);
		if (changed) {
			repeatedSignal = getInputSignal();
			ticks = 0;
			gate.scheduleTick(signalValues[valueMode] * 2 - 1);
		}
	}

	@Override
	public boolean tick(PartGate gate) {
		if (ticks == 2) {
			boolean changed = gate.updateInputs(inputValues);
			if (changed) {
				repeatedSignal = getInputSignal();
				updateOutputs();
				return true;
			} else {
				return false;
			}
		} else {
			ticks++;
			gate.scheduleTick(signalValues[valueMode]);
			updateOutputs();
			return true;
		}
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		if (side == EnumFacing.NORTH) {
			if (repeatedSignal != 0) {
				return 15;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
}
