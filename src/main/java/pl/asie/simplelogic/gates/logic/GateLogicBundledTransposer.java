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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.stagingapi.IConfigurationHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class GateLogicBundledTransposer extends GateLogic implements ICapabilityProvider, IConfigurationHolder {
	public int[] transpositionMap = new int[16]; // int[from] & to
	private int tMapHash = 0;

	private boolean readTmap(NBTTagCompound tag) {
		if (tag.hasKey("tmap", Constants.NBT.TAG_INT_ARRAY)) {
			int[] oldTranspositionMap = transpositionMap;
			transpositionMap = ensureSizeAndCopy(tag.getIntArray("tmap"), 16);
			tMapHash = Arrays.hashCode(transpositionMap);
			return !Arrays.equals(oldTranspositionMap, transpositionMap);
		} else {
			return false;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag, boolean isClient) {
		tag = super.writeToNBT(tag, isClient);
		if (isClient) {
			tag.setIntArray("tmap", transpositionMap);
		} else {
			for (int i = 0; i < 16; i++) {
				if (transpositionMap[i] != 0) {
					tag.setIntArray("tmap", transpositionMap);
					break;
				}
			}
		}
		return tag;
	}

	@Override
	public NBTTagCompound writeItemNBT(NBTTagCompound tag, boolean silky) {
		tag = super.writeItemNBT(tag, silky);
		if (silky) {
			tag.setIntArray("tmap", transpositionMap);
		}
		return tag;
	}

	@Override
	public boolean readFromNBT(NBTTagCompound tag, boolean isClient) {
		return super.readFromNBT(tag, isClient) | readTmap(tag);
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

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == Capabilities.CONFIGURATION_HOLDER;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == Capabilities.CONFIGURATION_HOLDER ? Capabilities.CONFIGURATION_HOLDER.cast(this) : null;
	}

	@Override
	public ResourceLocation getConfigType() {
		return new ResourceLocation("simplelogic:bundled_transposer");
	}

	@Override
	public NBTTagCompound serializeConfig() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setIntArray("tmap", transpositionMap);
		return tag;
	}

	@Override
	public DeserializationResult deserializeConfig(NBTTagCompound compound, ResourceLocation type) {
		if (compound.hasKey("tmap", Constants.NBT.TAG_INT_ARRAY)) {
			return readTmap(compound) ? DeserializationResult.CHANGED_ACCURATE : DeserializationResult.UNCHANGED;
		} else {
			return DeserializationResult.INVALID;
		}
	}
}
