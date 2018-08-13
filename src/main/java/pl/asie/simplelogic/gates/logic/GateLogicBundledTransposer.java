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

import java.util.Arrays;

public class GateLogicBundledTransposer extends GateLogic {
	public int[] transpositionMap = new int[16]; // int[from] & to
	private int tMapHash = 0;

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		super.writeToNBT(tag, isClient);
		tag.setIntArray("tmap", transpositionMap);
		return tag;
	}

	@Override
	public boolean readFromNBT(NBTTagCompound tag, boolean isClient) {
		boolean update = super.readFromNBT(tag, isClient);
		if (tag.hasKey("tmap", Constants.NBT.TAG_INT_ARRAY)) {
			int[] oldTranspositionMap = transpositionMap;
			transpositionMap = ensureSizeAndCopy(tag.getIntArray("tmap"), 16);
			tMapHash = Arrays.hashCode(transpositionMap);
			update |= !Arrays.equals(oldTranspositionMap, transpositionMap);
		}
		return update;
	}

	@Override
	public void onChanged(IGateContainer gate) {
		byte[] input = getInputValueBundled(EnumFacing.SOUTH);
		byte[] oldOutput = getOutputValueBundled(EnumFacing.NORTH);
		byte[] data = new byte[16];

		if (input == null) {
			for (int i = 0; i < 16; i++)
				data[i] = 0;
		} else {
			for (int from = 0; from < 16; from++) {
				int v = transpositionMap[from];
				int i = 0;
				while (v != 0) {
					if ((v & 1) != 0) {
						data[i] = (byte) Math.max(data[i], input[from]);
					}
					v >>= 1;
					i++;
				}
			}
		}

		if (!Arrays.equals(oldOutput, data)) {
			outputValuesBundled[0] = data;
			gate.markGateChanged(true);
		}
	}

	public void onTMapChanged(IGateContainer gate) {
		tMapHash = Arrays.hashCode(transpositionMap);
		gate.markGateChanged(false);
		onChanged(gate);
	}

	@Override
	public boolean onRightClick(IGateContainer gate, EntityPlayer playerIn, Vec3d vec, EnumHand hand) {
		if (playerIn.isSneaking()) {
			gate.openGUI(playerIn);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean updateOutputs(IGateContainer gate) {
		return false;
	}

	@Override
	public GateRenderState getLayerState(int id) {
		return GateRenderState.OFF;
	}

	@Override
	public GateRenderState getTorchState(int id) {
		return GateRenderState.OFF;
	}

	@Override
	public GateConnection getType(EnumFacing dir) {
		switch (dir) {
			case SOUTH:
				return GateConnection.INPUT_BUNDLED;
			case NORTH:
				return GateConnection.OUTPUT_BUNDLED;
			default:
				return GateConnection.NONE;
		}
	}

	@Override
	public boolean renderEquals(GateLogic other) {
		if (!(other instanceof GateLogicBundledTransposer)) {
			return false;
		} else {
			return Arrays.equals(((GateLogicBundledTransposer) other).transpositionMap, transpositionMap);
		}
	}

	@Override
	public int renderHashCode(int hash) {
		return hash * 31 + tMapHash;
	}
}
