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
import net.minecraftforge.common.util.Constants;
import pl.asie.simplelogic.gates.PartGate;

import java.util.Arrays;

public abstract class GateLogic {
	public enum Connection {
		NONE,
		INPUT,
		OUTPUT,
		INPUT_OUTPUT,
		INPUT_ANALOG,
		OUTPUT_ANALOG,
		INPUT_BUNDLED,
		OUTPUT_BUNDLED,
		INPUT_OUTPUT_BUNDLED;

		public boolean isInput() {
			return this == INPUT || this == INPUT_ANALOG || this == INPUT_OUTPUT || this == INPUT_BUNDLED || this == INPUT_OUTPUT_BUNDLED;
		}

		public boolean isOutput() {
			return this == OUTPUT || this == OUTPUT_ANALOG || this == INPUT_OUTPUT || this == OUTPUT_BUNDLED || this == INPUT_OUTPUT_BUNDLED;
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
			return this == INPUT_BUNDLED || this == OUTPUT_BUNDLED || this == INPUT_OUTPUT_BUNDLED;
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
	protected byte[] inputValues = new byte[4];
	protected byte[] outputValues = new byte[4];

	public GateLogic() {
		enabledSides = getSideMask();
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		tag.setByte("le", enabledSides);
		tag.setByte("li", invertedSides);
		tag.setByteArray("lvi", inputValues);
		tag.setByteArray("lvo", outputValues);
		return tag;
	}

	public NBTTagCompound writeItemNBT(NBTTagCompound tag, boolean silky) {
		if (silky) {
			tag.setByte("le", enabledSides);
		}
		tag.setByte("li", invertedSides);
		return tag;
	}

	// Return true if a rendering update is in order.
	public boolean readFromNBT(NBTTagCompound compound, boolean isClient) {
		byte oldES = enabledSides;
		byte oldIS = invertedSides;
		byte[] oldIV = inputValues;
		byte[] oldOV = outputValues;

		if (compound.hasKey("le", Constants.NBT.TAG_ANY_NUMERIC)) {
			enabledSides = compound.getByte("le");
		}
		if (compound.hasKey("li", Constants.NBT.TAG_ANY_NUMERIC)) {
			invertedSides = compound.getByte("li");
		}

		if (compound.hasKey("lvi", Constants.NBT.TAG_BYTE_ARRAY)) {
			inputValues = compound.getByteArray("lvi");
		}
		if (compound.hasKey("lvo", Constants.NBT.TAG_BYTE_ARRAY)) {
			outputValues = compound.getByteArray("lvo");
		}

		if (inputValues == null || inputValues.length != 4) {
			inputValues = new byte[4];
		}
		if (outputValues == null || outputValues.length != 4) {
			outputValues = new byte[4];
		}

		if (!isClient) {
			if (compound.hasKey("lv", Constants.NBT.TAG_BYTE_ARRAY)) {
				// Compat code
				byte[] values = compound.getByteArray("lv");
				for (EnumFacing facing : EnumFacing.HORIZONTALS) {
					Connection c = getType(facing);
					if (c.isOutput()) {
						inputValues[facing.ordinal() - 2] = 0;
						outputValues[facing.ordinal() - 2] = values[facing.ordinal() - 2];
					} else {
						inputValues[facing.ordinal() - 2] = values[facing.ordinal() - 2];
						outputValues[facing.ordinal() - 2] = 0;
					}
				}
			}
		}

		return oldES != enabledSides || oldIS != invertedSides || !Arrays.equals(oldIV, inputValues) || !Arrays.equals(oldOV, outputValues);
	}

	public boolean updateOutputs() {
		byte[] oldValues = new byte[4];
		boolean changed = false;

		System.arraycopy(outputValues, 0, oldValues, 0, 4);

		for (int i = 0; i <= 3; i++) {
			EnumFacing facing = EnumFacing.byIndex(i + 2);
			GateLogic.Connection conn = getType(facing);
			if (conn.isOutput() && conn.isRedstone()) {
				outputValues[i] = calculateOutputInside(facing);
			} else {
				outputValues[i] = 0;
			}

			if (outputValues[i] != oldValues[i]) {
				changed = true;
			}
		}

		return changed;
	}

	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.NORTH ? Connection.OUTPUT : Connection.INPUT;
	}

	public abstract State getLayerState(int id);

	public abstract State getTorchState(int id);

	public boolean canMirror() {
		return getType(EnumFacing.WEST) != Connection.NONE || getType(EnumFacing.EAST) != Connection.NONE;
	}

	public boolean canBlockSide(EnumFacing side) {
		return getType(side).isInput() && !getType(side).isBundled();
	}

	public boolean canInvertSide(EnumFacing side) {
		return getType(side).isDigital();
	}

	protected final byte getSideMask() {
		byte j = 0;
		for (int i = 0; i <= 3; i++) {
			if (getType(EnumFacing.byIndex(i + 2)) != Connection.NONE) {
				j |= (1 << i);
			}
		}
		return j;
	}

	protected abstract byte calculateOutputInside(EnumFacing side);

	public boolean onRightClick(PartGate gate, EntityPlayer playerIn, EnumHand hand) {
		return false;
	}

	public final boolean isSideOpen(EnumFacing side) {
		return (enabledSides & (1 << (side.ordinal() - 2))) != 0;
	}

	public final boolean isSideInverted(EnumFacing side) {
		return (invertedSides & (1 << (side.ordinal() - 2))) != 0;
	}

	public final byte getInputValueOutside(EnumFacing side) {
		if (isSideInverted(side) && isSideOpen(side)) {
			return inputValues[side.ordinal() - 2] != 0 ? 0 : (byte) 15;
		} else {
			return inputValues[side.ordinal() - 2];
		}
	}

	public final byte getInputValueInside(EnumFacing side) {
		return inputValues[side.ordinal() - 2];
	}

	public final byte getOutputValueInside(EnumFacing side) {
		return outputValues[side.ordinal() - 2];
	}

	public final byte getOutputValueOutside(EnumFacing side) {
		if (isSideInverted(side) && isSideOpen(side)) {
			return outputValues[side.ordinal() - 2] != 0 ? 0 : (byte) 15;
		} else {
			return outputValues[side.ordinal() - 2];
		}
	}

	public byte[] getOutputValueBundled(EnumFacing side) {
		throw new RuntimeException("You should implement this yourself!");
	}

	public void onChanged(PartGate gate) {
		gate.scheduleTick();
	}

	public boolean tick(PartGate gate) {
		boolean inputChange = gate.updateInputs(inputValues);
		boolean outputChange = updateOutputs();
		return inputChange || outputChange;
	}

	public boolean renderEquals(GateLogic other) {
		return true;
	}

	public int renderHashCode(int hash) {
		return hash;
	}

	// Utility methods

	protected final boolean rsToDigi(byte v) {
		return v > 0;
	}

	protected final byte digiToRs(boolean v) {
		return v ? (byte) 15 : 0;
	}
}
