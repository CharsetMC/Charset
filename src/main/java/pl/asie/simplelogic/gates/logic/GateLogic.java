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
import net.minecraftforge.common.util.Constants;
import pl.asie.simplelogic.gates.PartGate;

public abstract class GateLogic {
	public enum Connection {
		NONE,
		INPUT,
		OUTPUT,
		INPUT_OUTPUT,
		INPUT_ANALOG,
		OUTPUT_ANALOG,
		INPUT_BUNDLED,
		OUTPUT_BUNDLED;

		public boolean isInput() {
			return this == INPUT || this == INPUT_ANALOG || this == INPUT_OUTPUT || this == INPUT_BUNDLED;
		}

		public boolean isOutput() {
			return this == OUTPUT || this == OUTPUT_ANALOG || this == INPUT_OUTPUT || this == OUTPUT_BUNDLED;
		}

		public boolean isRedstone() {
			return this == INPUT || this == OUTPUT || this == INPUT_ANALOG || this == OUTPUT_ANALOG || this == INPUT_OUTPUT;
		}

		public boolean isDigital() {
			return this == INPUT || this == OUTPUT || this == INPUT_OUTPUT;
		}

		public boolean isAnalog() {
			return this == INPUT_ANALOG || this == OUTPUT_ANALOG;
		}

		public boolean isBundled() {
			return this == INPUT_BUNDLED || this == OUTPUT_BUNDLED;
		}
	}

	public enum State {
		NO_RENDER,
		OFF,
		ON,
		DISABLED;

		public State invert() {
			switch (this) {
				case OFF:
					return ON;
				case ON:
					return OFF;
				default:
					return this;
			}
		}

		public static State input(byte i) {
			return i > 0 ? ON : OFF;
		}

		public static State bool(boolean v) {
			return v ? ON : OFF;
		}
	}
	
	public byte enabledSides, invertedSides;
	private byte[] values = new byte[4];

	public GateLogic() {
		enabledSides = getSideMask();
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setByte("le", enabledSides);
		tag.setByte("li", invertedSides);
		tag.setByteArray("lv", values);
		return tag;
	}

	public NBTTagCompound writeItemNBT(NBTTagCompound tag, boolean silky) {
		if (silky) {
			tag.setByte("le", enabledSides);
		}
		tag.setByte("li", invertedSides);
		return tag;
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("lv", Constants.NBT.TAG_BYTE_ARRAY)) {
			values = compound.getByteArray("lv");
		}
		if (values == null || values.length != 4) {
			values = new byte[4];
		}

		if (compound.hasKey("le", Constants.NBT.TAG_ANY_NUMERIC)) {
			enabledSides = compound.getByte("le");
		}
		if (compound.hasKey("li", Constants.NBT.TAG_ANY_NUMERIC)) {
			invertedSides = compound.getByte("li");
		}
	}

	public byte[] getValues() {
		return values;
	}

	public boolean updateOutputs() {
		byte[] oldValues = new byte[4];
		boolean changed = false;

		System.arraycopy(values, 0, oldValues, 0, 4);

		for (int i = 0; i <= 3; i++) {
			EnumFacing facing = EnumFacing.getFront(i + 2);
			GateLogic.Connection conn = getType(facing);
			if (conn.isOutput() && conn.isRedstone()) {
				values[i] = calculateOutputInside(facing);
			}

			if (values[i] != oldValues[i]) {
				changed = true;
			}
		}

		return changed;
	}

	protected boolean rsToDigi(byte v) {
		return v > 0;
	}

	protected byte digiToRs(boolean v) {
		return v ? (byte) 15 : 0;
	}

	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.NORTH ? Connection.OUTPUT : Connection.INPUT;
	}

	public abstract State getLayerState(int id);

	public abstract State getTorchState(int id);

	public boolean canBlockSide(EnumFacing side) {
		return getType(side).isInput();
	}

	public boolean canInvertSide(EnumFacing side) {
		return getType(side).isDigital();
	}

	protected byte getSideMask() {
		byte j = 0;
		for (int i = 0; i <= 3; i++) {
			if (getType(EnumFacing.getFront(i + 2)) != Connection.NONE) {
				j |= (1 << i);
			}
		}
		return j;
	}

	protected abstract byte calculateOutputInside(EnumFacing side);

	public boolean isSideOpen(EnumFacing side) {
		return (enabledSides & (1 << (side.ordinal() - 2))) != 0;
	}

	public boolean isSideInverted(EnumFacing side) {
		return (invertedSides & (1 << (side.ordinal() - 2))) != 0;
	}

	public byte getValueInside(EnumFacing side) {
		return values[side.ordinal() - 2];
	}

	public byte getValueOutside(EnumFacing side) {
		if (isSideInverted(side) && isSideOpen(side)) {
			return values[side.ordinal() - 2] != 0 ? 0 : (byte) 15;
		} else {
			return values[side.ordinal() - 2];
		}
	}

	public void onChanged(PartGate parent) {
		parent.scheduleTick();
	}

	public boolean tick(PartGate parent) {
		return parent.updateInputs();
	}
}
