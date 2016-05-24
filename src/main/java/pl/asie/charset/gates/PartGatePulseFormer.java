/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.gates;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class PartGatePulseFormer extends PartGate {
	private byte pulse;

	public PartGatePulseFormer() {
		super();
	}

	@Override
	public boolean canBlockSide(EnumFacing side) {
		return false;
	}

	@Override
	public boolean canInvertSide(EnumFacing side) {
		return side == EnumFacing.SOUTH;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("pl", pulse);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		pulse = tag.getByte("pl");
		super.readFromNBT(tag);
	}

	@Override
	protected void onChanged() {
		if (pulse == 0) {
			boolean changed = super.tick();
			if (changed) {
				pulse = getValueInside(EnumFacing.SOUTH);
				if (pulse != 0) {
					scheduleTick();
				}
				notifyBlockUpdate();
				sendUpdatePacket();
			}
		}
	}

	@Override
	protected boolean tick() {
		boolean changed = pulse != 0;
		pulse = 0;
		changed |= super.tick();
		return changed;
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
		boolean hasSignal = getValueInside(EnumFacing.SOUTH) != 0;
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.SOUTH));
			case 1:
			case 2:
				return State.bool(!hasSignal);
			case 3:
				return State.bool(hasSignal);
			case 4:
				return State.input(getValueOutside(EnumFacing.NORTH));
		}
		return State.OFF;
	}

	@Override
	public State getTorchState(int id) {
		switch (id) {
			case 0:
				return State.input(getValueInside(EnumFacing.SOUTH)).invert();
			case 1:
				return State.input(getValueInside(EnumFacing.SOUTH));
			case 2:
				return State.input(getValueInside(EnumFacing.NORTH)).invert();
		}
		return State.ON;
	}

	@Override
	protected byte calculateOutputInside(EnumFacing side) {
		return pulse;
	}
}
