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

public class GateLogicRSLatch extends GateLogic {
	private boolean toggled;
	private boolean burnt;

	@Override
	public boolean tick(IGateContainer gate) {
		boolean oldIS = getOutputValueInside(EnumFacing.WEST) != 0;
		boolean oldIR = getOutputValueInside(EnumFacing.EAST) != 0;

		boolean newIS = getInputValueInside(EnumFacing.WEST) != 0;
		boolean newIR = getInputValueInside(EnumFacing.EAST) != 0;

		int state = ((!oldIR && newIR) ? 1 : 0) | ((!oldIS && newIS) ? 2 : 0);
		boolean oldToggled = toggled;

		switch (state) {
			case 0:
			default:
				break;
			case 1:
				toggled = false;
				break;
			case 2:
				toggled = true;
				break;
			case 3:
				//burnt = true;
				//BlockPos pos = parent.getPos();
				//parent.getWorld().playSound(pos.getX() + 0.5F, pos.getY() + 0.1F, pos.getZ() + 0.5F,
				//		new SoundEvent(new ResourceLocation("random.fizz")), SoundCategory.BLOCKS, 0.5F, 2.6F + (parent.getWorld().rand.nextFloat() - parent.getWorld().rand.nextFloat()) * 0.8F, true);

				// haha, JK
				toggled = !toggled;
				break;
		}

		return oldToggled != toggled;
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
	public GateConnection getType(EnumFacing dir) {
		return dir.getAxis() == EnumFacing.Axis.X ? GateConnection.INPUT_OUTPUT : GateConnection.OUTPUT;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		super.writeToNBT(tag, isClient);
		tag.setBoolean("tg", toggled);
		tag.setBoolean("br", burnt);
		return tag;
	}

	@Override
	public boolean readFromNBT(NBTTagCompound tag, boolean isClient) {
		boolean oldTg = toggled;
		boolean oldBt = burnt;
		toggled = tag.getBoolean("tg");
		burnt = tag.getBoolean("br");
		return super.readFromNBT(tag, isClient) || (oldTg != toggled) || (oldBt != burnt);
	}

	@Override
	public GateRenderState getLayerState(int id) {
		if (burnt) {
			return GateRenderState.OFF;
		}
		switch (id) {
			case 1:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
			case 0:
				return GateRenderState.input(getOutputValueInside(EnumFacing.SOUTH));
		}
		return null;
	}

	@Override
	public GateRenderState getTorchState(int id) {
		if (burnt) {
			return GateRenderState.OFF;
		}
		switch (id) {
			case 0:
				return GateRenderState.input(getOutputValueInside(EnumFacing.NORTH));
			case 1:
				return GateRenderState.input(getOutputValueInside(EnumFacing.SOUTH));
		}
		return null;
	}

	@Override
	public byte calculateOutputInside(EnumFacing facing) {
		if (burnt) {
			return 0;
		}
		return (toggled ^ (facing == EnumFacing.NORTH || facing == EnumFacing.EAST)) ? (byte) 15 : 0;
	}
}
