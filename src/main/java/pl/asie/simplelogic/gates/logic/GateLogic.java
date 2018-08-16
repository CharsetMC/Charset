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
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class GateLogic {
	public byte enabledSides, invertedSides;
	protected byte[] outputValues = new byte[4];
	protected final byte[][] outputValuesBundled = new byte[4][];
	private byte[] inputValues = new byte[4];
	private final byte[][] inputValuesBundled = new byte[4][];

	public GateLogic() {
		enabledSides = getSideMask();
	}

	protected boolean shouldSyncBundledWithClient() {
		return false;
	}

	public boolean shouldTick() {
		return true;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		tag.setByte("le", enabledSides);
		tag.setByte("li", invertedSides);
		tag.setByteArray("lvi", inputValues);
		tag.setByteArray("lvo", outputValues);
		if (!isClient || shouldSyncBundledWithClient()) {
			for (int i = 0; i < 4; i++) {
				if (inputValuesBundled[i] != null) {
					tag.setByteArray("lbi" + i, inputValuesBundled[i]);
				}
				if (outputValuesBundled[i] != null) {
					tag.setByteArray("lbo" + i, outputValuesBundled[i]);
				}
			}
		}
		return tag;
	}

	public NBTTagCompound writeItemNBT(NBTTagCompound tag, boolean silky) {
		if (silky) {
			tag.setByte("le", enabledSides);
		}
		tag.setByte("li", invertedSides);
		return tag;
	}

	protected byte[] ensureSizeAndCopy(byte[] data, int len) {
		if (data.length != len) {
			return new byte[len];
		} else {
			return Arrays.copyOf(data, len);
		}
	}

	protected int[] ensureSizeAndCopy(int[] data, int len) {
		if (data.length != len) {
			return new int[len];
		} else {
			return Arrays.copyOf(data, len);
		}
	}

	// Return true if a rendering update is in order.
	public boolean readFromNBT(NBTTagCompound compound, boolean isClient) {
		boolean update = false;

		if (compound.hasKey("le", Constants.NBT.TAG_ANY_NUMERIC)) {
			byte old = enabledSides;
			enabledSides = compound.getByte("le");
			update = (enabledSides != old);
		}

		if (compound.hasKey("li", Constants.NBT.TAG_ANY_NUMERIC)) {
			byte old = invertedSides;
			invertedSides = compound.getByte("li");
			update = (invertedSides != old);
		}

		if (compound.hasKey("lvi", Constants.NBT.TAG_BYTE_ARRAY)) {
			byte[] old = inputValues;
			inputValues = ensureSizeAndCopy(compound.getByteArray("lvi"), 4);
			update |= !Arrays.equals(old, inputValues);
		}

		if (compound.hasKey("lvo", Constants.NBT.TAG_BYTE_ARRAY)) {
			byte[] old = outputValues;
			outputValues = ensureSizeAndCopy(compound.getByteArray("lvo"), 4);
			update |= !Arrays.equals(old, outputValues);
		}

		for (int i = 0; i < 4; i++) {
			if (compound.hasKey("lbi" + i, Constants.NBT.TAG_BYTE_ARRAY)) {
				byte[] old = inputValuesBundled[i];
				inputValuesBundled[i] = ensureSizeAndCopy(compound.getByteArray("lbi" + i), 16);
				update |= !Arrays.equals(old, inputValuesBundled[i]);
			}

			if (compound.hasKey("lbo" + i, Constants.NBT.TAG_BYTE_ARRAY)) {
				byte[] old = outputValuesBundled[i];
				outputValuesBundled[i] = ensureSizeAndCopy(compound.getByteArray("lbo" + i), 16);
				update |= !Arrays.equals(old, outputValuesBundled[i]);
			}
		}

		return update;
	}

	protected boolean updateInput(IGateContainer gate, EnumFacing facing) {
		int i = facing.ordinal() - 2;
		GateConnection conn = getType(facing);

		if (conn.isInput()) {
			byte oldOV = inputValues[i];
			byte[] oldOVB = inputValuesBundled[i];

			if (!isSideOpen(facing)) {
				inputValues[i] = 0;
				inputValuesBundled[i] = null;
			} else if (conn.isBundled()) {
				inputValues[i] = 0;
				inputValuesBundled[i] = gate.getBundledInput(facing);
			} else {
				inputValues[i] = gate.getRedstoneInput(facing);
				inputValuesBundled[i] = null;
			}

			return (oldOV != inputValues[i]) || !Arrays.equals(oldOVB, inputValuesBundled[i]);
		} else {
			return false;
		}
	}

	protected boolean updateOutput(EnumFacing facing) {
		int i = facing.ordinal() - 2;
		GateConnection conn = getType(facing);

		if (conn.isOutput()) {
			byte oldOV = outputValues[i];
			byte[] oldOVB = outputValuesBundled[i];

			if (!isSideOpen(facing)) {
				outputValues[i] = 0;
				outputValuesBundled[i] = null;
			} else if (conn.isBundled()) {
				outputValues[i] = 0;
				outputValuesBundled[i] = new byte[16];
				calculateOutputBundled(facing, outputValuesBundled[i]);
			} else {
				outputValues[i] = calculateOutputInside(facing);
				outputValuesBundled[i] = null;
			}

			return (oldOV != outputValues[i]) || !Arrays.equals(oldOVB, outputValuesBundled[i]);
		} else {
			return false;
		}
	}

	// Order: updateInputs -true-> onChanged -scheduled-> tick -true-> updateOutputs

	public boolean updateInputs(IGateContainer gate) {
		boolean changed = false;

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			changed |= updateInput(gate, facing);
		}

		return changed;
	}

	public void onChanged(IGateContainer gate) {
		gate.scheduleRedstoneTick();
	}

	public boolean tick(IGateContainer gate) {
		return true;
	}

	public boolean updateOutputs(IGateContainer gate) {
		boolean changed = false;

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			changed |= updateOutput(facing);
		}

		return changed;
	}

	protected byte calculateOutputInside(EnumFacing side) {
		throw new RuntimeException("Implement me!");
	}

	protected void calculateOutputBundled(EnumFacing side, @Nonnull byte[] data) {
		throw new RuntimeException("Implement me!");
	}

	public GateConnection getType(EnumFacing dir) {
		return dir == EnumFacing.NORTH ? GateConnection.OUTPUT : GateConnection.INPUT;
	}

	public abstract GateRenderState getLayerState(int id);

	public abstract GateRenderState getTorchState(int id);

	public boolean hasComparatorInputs() {
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (getType(facing).isComparator()) {
				return true;
			}
		}

		return false;
	}

	public boolean canMirror() {
		return getType(EnumFacing.WEST) != GateConnection.NONE || getType(EnumFacing.EAST) != GateConnection.NONE;
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
			if (getType(EnumFacing.byIndex(i + 2)) != GateConnection.NONE) {
				j |= (1 << i);
			}
		}
		return j;
	}

	public boolean onRightClick(IGateContainer gate, EntityPlayer playerIn, Vec3d vec, EnumHand hand) {
		return false;
	}

	public final boolean isSideOpen(EnumFacing side) {
		return (enabledSides & (1 << (side.ordinal() - 2))) != 0;
	}

	public final boolean isSideInverted(EnumFacing side) {
		return (invertedSides & (1 << (side.ordinal() - 2))) != 0;
	}

	@Nullable
	public final byte[] getInputValueBundled(EnumFacing side) {
		return inputValuesBundled[side.ordinal() - 2];
	}

	@Nullable
	public final byte[] getOutputValueBundled(EnumFacing side) {
		return outputValuesBundled[side.ordinal() - 2];
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

	public boolean renderEquals(GateLogic other) {
		return true;
	}

	public int renderHashCode(int hash) {
		return hash;
	}
}
