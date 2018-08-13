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
import pl.asie.simplelogic.gates.SimpleLogicGates;

public class GateLogicTimer extends GateLogic implements IArrowGateLogic, IGateTickable {
	private int ticksTotal = 20;
	private int ticks;

	@Override
	public Connection getType(EnumFacing side) {
		switch (side) {
			case EAST:
			case WEST:
				return Connection.OUTPUT;
			case SOUTH:
				return Connection.INPUT;
			case NORTH:
			default:
				return Connection.OUTPUT;
		}
	}

	@Override
	public boolean onRightClick(PartGate gate, EntityPlayer playerIn, Vec3d vec, EnumHand hand) {
		if (playerIn.isSneaking()) {
			gate.openGUI(playerIn);
			return true;
		} else {
			String sec;
			if ((ticksTotal % 20) != 0) {
				if ((ticksTotal & 1) != 0) {
					sec = String.format("%d.%02d", (ticksTotal / 20) % 60, (ticksTotal % 20) * 5);
				} else {
					sec = String.format("%d.%d", (ticksTotal / 20) % 60, (ticksTotal % 20) / 2);
				}
			} else {
				sec = String.format("%d", (ticksTotal / 20) % 60);
			}

			if (ticksTotal >= 1200) {
				String min = String.format("%d", (ticksTotal / 1200) % 60);
				if ((ticksTotal % 1200) == 0) {
					new Notice(gate, NotificationComponentString.translated("notice.simplelogic.gate.timer.minutes",
							NotificationComponentString.raw(min)
					)).sendTo(playerIn);
				} else {
					new Notice(gate, NotificationComponentString.translated("notice.simplelogic.gate.timer.minutes_seconds",
							NotificationComponentString.raw(min),
							NotificationComponentString.raw(sec)
					)).sendTo(playerIn);
				}
			} else {
				new Notice(gate, NotificationComponentString.translated("notice.simplelogic.gate.timer.seconds",
						NotificationComponentString.raw(sec)
				)).sendTo(playerIn);
			}
			return true;
		}
	}

	public int clampTicksTotal(int tt) {
		return MathHelper.clamp(tt, SimpleLogicGates.minTimerTickTime, 24000);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		super.writeToNBT(tag, isClient);
		tag.setInteger("rt", ticks);
		tag.setInteger("rtt", ticksTotal);
		return tag;
	}

	@Override
	public boolean readFromNBT(NBTTagCompound tag, boolean isClient) {
		int oldT = ticks;
		int oldTT = ticksTotal;
		ticks = tag.getInteger("rt");
		if (!tag.hasKey("rtt")) {
			ticksTotal = clampTicksTotal(20);
		} else {
			ticksTotal = clampTicksTotal(tag.getInteger("rtt"));
		}

		if (ticks >= ticksTotal || ticks < 0) {
			ticks = 0;
		}

		return super.readFromNBT(tag, isClient) || (oldT != ticks) || (oldTT != ticksTotal);
	}

	private boolean isStopped() {
		return (getInputValueInside(EnumFacing.SOUTH) != 0);
	}

	private boolean isPowered() {
		return ticks < 2;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return side == EnumFacing.SOUTH;
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return side.getAxis() == EnumFacing.Axis.X;
	}

	@Override
	public State getLayerState(int id) {
		switch (id) {
			case 0:
				return State.input(getInputValueInside(EnumFacing.SOUTH));
			case 1:
			default:
				return State.bool(isPowered());
		}
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
			default:
				return State.bool(isPowered());
			case 1:
				return State.ON;
		}
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		return (byte) (isPowered() ? 15 : 0);
	}

	@Override
	public float getArrowPosition() {
		return (float) ticks / ticksTotal;
	}

	@Override
	public float getArrowRotationDelta() {
		if (isStopped()) {
			return 0;
		} else {
			return (float) 1 / ticksTotal;
		}
	}

	@Override
	public void update(PartGate gate) {
		int oldTicks = ticks;
		if (isStopped()) {
			ticks = 0;
		} else {
			ticks = (ticks + 1) % ticksTotal;
		}

		if (oldTicks != ticks && ticks < 3) {
			if (!gate.getWorld().isRemote) {
				if (updateOutputs()) {
					gate.propagateOutputs();
				}
			} else if (ticks == 0 || ticks == 2) {
				gate.markBlockForRenderUpdate();
			}
		}
	}

	public int getTicksTotal() {
		return ticksTotal;
	}

	public void setTicksTotal(PartGate gate, int ch) {
		int newTicksTotal = clampTicksTotal(ch);
		if (newTicksTotal != ticksTotal) {
			ticksTotal = newTicksTotal;
			if (!gate.getWorld().isRemote) {
				gate.markChunkDirty();
				gate.markBlockForUpdate();
			} else {
				gate.markBlockForRenderUpdate();
			}
		}
	}
}
