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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.component.NotificationComponentString;
import pl.asie.simplelogic.gates.PartGate;

public class GateLogicComparator extends GateLogic {
	private byte mode = 0;

	@Override
	public Connection getType(EnumFacing dir) {
		switch (dir) {
			case NORTH:
				return Connection.OUTPUT_ANALOG;
			case WEST:
			case EAST:
				return Connection.INPUT_ANALOG;
			case SOUTH:
				return Connection.INPUT_COMPARATOR;
			default:
				return Connection.NONE;
		}
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return side.getAxis() == EnumFacing.Axis.X;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		super.writeToNBT(tag, isClient);
		tag.setByte("gm", mode);
		return tag;
	}

	@Override
	public boolean readFromNBT(NBTTagCompound tag, boolean isClient) {
		byte oldMode = mode;
		mode = tag.getByte("gm");
		return super.readFromNBT(tag, isClient) || (mode != oldMode);
	}

	@Override
	public boolean onRightClick(PartGate gate, EntityPlayer playerIn, Vec3d vec, EnumHand hand) {
		if (playerIn.isSneaking()) {
			mode = (byte) (1 - mode);
			gate.markBlockForUpdate();
		}

		new Notice(gate, NotificationComponentString.translated("notice.simplelogic.gate.comparator.mode." + mode))
				.sendTo(playerIn);
		return true;
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
			case 1:
				return State.input(getInputValueInside(EnumFacing.SOUTH));
			case 2:
				return State.input(getInputValueInside(EnumFacing.WEST));
			case 3:
				return State.input(getInputValueInside(EnumFacing.EAST));
			default:
				return State.OFF;
		}
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
			case 1:
				return State.input(getInputValueInside(EnumFacing.SOUTH));
			case 2:
				return mode == 0 ? State.OFF : State.NO_RENDER;
			case 3:
				return mode == 1 ? State.ON : State.NO_RENDER;
			default:
				return State.OFF;
		}
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		if (side == EnumFacing.NORTH) {
			int maxSide = Math.max(getInputValueInside(EnumFacing.WEST), getInputValueInside(EnumFacing.EAST));
			int rear = getInputValueInside(EnumFacing.SOUTH);
			switch (mode) {
				case 0: // Compare
				default:
					return (byte) (maxSide > rear ? 0 : rear);
				case 1: // Subtract
					return (byte) MathHelper.clamp(rear - maxSide, 0, 15);
			}
		} else {
			return 0;
		}
	}
}
