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

package pl.asie.simplelogic.wires.logic;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.charset.lib.wires.WireUtils;
import pl.asie.simplelogic.wires.OldWireUtils;

import javax.annotation.Nonnull;

public class PartWireBundled extends PartWireSignalBase implements IBundledReceiver, IBundledEmitter {
	private int[] signalLevel = new int[16];
	private byte[] signalValue = new byte[16];

	public PartWireBundled(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		return -1;
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		super.readNBTData(nbt, isClient);
		signalLevel = nbt.getIntArray("s");
		if (signalLevel == null || signalLevel.length != 16) {
			signalLevel = new int[16];
		}
		for (int i = 0; i < 16; i++) {
			signalValue[i] = (byte) (signalLevel[i] >> 8);
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
		super.writeNBTData(nbt, isClient);
		nbt.setIntArray("s", signalLevel);
		return nbt;
	}

	private void propagate(int color, byte[][] nValues) {
		int maxSignal = 0;
		int[] neighborLevel = new int[7];
		boolean[] isWire = new boolean[7];

		PartWireSignalBase.PROPAGATING = true;

		for (WireFace location : WireFace.VALUES) {
			if (connectsInternal(location)) {
				isWire[location.ordinal()] = true;
				neighborLevel[location.ordinal()] = OldWireUtils.getBundledWireLevel(getContainer().world(), getContainer().pos(), location, color);
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (connectsExternal(facing)) {
				if (nValues[facing.ordinal()] != null) {
					int v = nValues[facing.ordinal()][color] << 8;
					if (v != 0) {
						neighborLevel[facing.ordinal()] = v | 0xFF;
					}
				} else {
					BlockPos pos = getContainer().pos().offset(facing);
					Wire wire = WireUtils.getWire(getContainer().world(), pos, getLocation());

					if (wire instanceof PartWireSignalBase) {
						isWire[facing.ordinal()] = true;
						neighborLevel[facing.ordinal()] = OldWireUtils.getBundledWireLevel(getContainer().world(), pos, getLocation(), color);
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = getContainer().pos().offset(facing).offset(getLocation().facing);
				Wire wire = WireUtils.getWire(getContainer().world(), cornerPos, getLocation());

				if (wire instanceof PartWireSignalBase) {
					isWire[facing.ordinal()] = true;
					neighborLevel[facing.ordinal()] = OldWireUtils.getBundledWireLevel(getContainer().world(), cornerPos, WireFace.get(facing.getOpposite()), color);
				}
			}
		}

		PartWireSignalBase.PROPAGATING = false;

		int newSignal = 0;

		for (int j = 0; j < 7; j++) {
			if (neighborLevel[j] > maxSignal) {
				maxSignal = neighborLevel[j];
			}
			if (!isWire[j] && neighborLevel[j] > newSignal) {
				newSignal = neighborLevel[j];
			}
		}

		if (DEBUG) {
			System.out.println("[" + color + "] Levels: " + Arrays.toString(neighborLevel));
		}

		if (maxSignal > signalLevel[color] && maxSignal > 1) {
			newSignal = maxSignal - 1;
			if ((newSignal & 0xFF) == 0 || (newSignal & 0xFF) == 0xFF) {
				newSignal = 0;
			}
		}

		if (newSignal == signalLevel[color]) {
			return;
		}

		signalLevel[color] = newSignal;
		signalValue[color] = (byte) (newSignal >> 8);

		if (newSignal == 0) {
			for (WireFace nLoc : WireFace.VALUES) {
				if (connectsInternal(nLoc)) {
					if (neighborLevel[nLoc.ordinal()] > 0) {
						Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos(), nLoc);
						if (wire instanceof PartWireSignalBase) ((PartWireSignalBase) wire).onSignalChanged(color);
					}
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing;

					if (connectsExternal(facing)) {
						Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos().offset(facing), getLocation());
						if (!(wire instanceof PartWireSignalBase) || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing, color);
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[facing.ordinal()] > 0) {
							propagateNotifyCorner(getLocation().facing, facing, color);
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				if (neighborLevel[nLoc.ordinal()] < newSignal - 1 || neighborLevel[nLoc.ordinal()] > (newSignal + 1)) {
					if (connectsInternal(nLoc)) {
						Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos(), nLoc);
						if (wire instanceof PartWireSignalBase) ((PartWireSignalBase) wire).onSignalChanged(color);
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing;

						if (connectsExternal(facing)) {
							propagateNotify(facing, color);
						} else if (connectsCorner(facing)) {
							propagateNotifyCorner(getLocation().facing, facing, color);
						}
					}
				}
			}
		}

		finishPropagation();
	}

	@Override
	public void propagate(int color) {
		if (DEBUG) {
			System.out.println("--- B! PROPAGATE " + getContainer().pos().toString() + " " + getLocation().name() + " --- " + color);
// TODO			System.out.println("ConnectionCache: " + Integer.toBinaryString(internalConnections) + " " + Integer.toBinaryString(externalConnections) + " " + Integer.toBinaryString(cornerConnections));
		}

		byte[][] nValues = new byte[6][];

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (connectsExternal(facing)) {
				BlockPos posFacing = getContainer().pos().offset(facing);
				IBundledEmitter emitter = null;

				if (WireUtils.hasCapability(this, getContainer().pos(), Capabilities.BUNDLED_EMITTER, facing.getOpposite(), false)) {
					emitter = WireUtils.getCapability(this, getContainer().pos(), Capabilities.BUNDLED_EMITTER, facing.getOpposite(), false);
				}

				if (emitter != null && !(emitter instanceof IWire)) {
					nValues[facing.ordinal()] = emitter.getBundledSignal();
				}
			}
		}

		if (color < 0) {
			for (int i = 0; i < 16; i++) {
				propagate(i, nValues);
			}
		} else {
			propagate(color, nValues);
		}
	}

	@Override
	protected void onSignalChanged(int color) {
		if (getContainer().world() != null && getContainer().pos() != null && !getContainer().world().isRemote) {
			propagate(color);
		}
	}

	@Override
	public int getBundledSignalLevel(int i) {
		return signalLevel[i & 15];
	}

	@Override
	public int getSignalLevel() {
		return 0;
	}

	@Override
	public int getRedstoneLevel() {
		return 0;
	}

	@Override
	public byte[] getBundledSignal() {
		return signalValue;
	}

	@Override
	public void onBundledInputChange() {
		scheduleLogicUpdate();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing face) {
		if (capability == Capabilities.BUNDLED_RECEIVER) {
			return connects(face);
		}
		if (capability == Capabilities.BUNDLED_EMITTER) {
			return connects(face);
		}
		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing face) {
		if (capability == Capabilities.BUNDLED_RECEIVER) {
			return (T) this;
		}
		if (capability == Capabilities.BUNDLED_EMITTER) {
			return (T) this;
		}
		return null;
	}
}
