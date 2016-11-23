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

package pl.asie.charset.wires.logic;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.wires.WireUtils;

public class PartWireBundled extends PartWireSignalBase implements IBundledReceiver, IBundledEmitter {
	private int[] signalLevel = new int[16];
	private byte[] signalValue = new byte[16];

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		return -1;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		signalLevel = nbt.getIntArray("s");
		if (signalLevel == null || signalLevel.length != 16) {
			signalLevel = new int[16];
		}
		for (int i = 0; i < 16; i++) {
			signalValue[i] = (byte) (signalLevel[i] >> 8);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setIntArray("s", signalLevel);
		return nbt;
	}

	private void propagate(int color, byte[][] nValues) {
		int maxSignal = 0;
		int[] neighborLevel = new int[7];
		boolean[] isWire = new boolean[7];

		PartWireSignalBase.PROPAGATING = true;

		if (internalConnections > 0) {
			for (WireFace location : WireFace.VALUES) {
				if (connectsInternal(location)) {
					isWire[location.ordinal()] = true;
					neighborLevel[location.ordinal()] = WireUtils.getBundledWireLevel(getContainer(), location, color);
				}
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
					IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing));
					if (container != null) {
						isWire[facing.ordinal()] = true;
						neighborLevel[facing.ordinal()] = WireUtils.getBundledWireLevel(container, location, color);
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = getPos().offset(facing).offset(location.facing);
				IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), cornerPos);
				if (container != null) {
					isWire[facing.ordinal()] = true;
					neighborLevel[facing.ordinal()] = WireUtils.getBundledWireLevel(container, WireFace.get(facing.getOpposite()), color);
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
						WireUtils.getWire(getContainer(), nLoc).onSignalChanged(color);
					}
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing;

					if (connectsExternal(facing)) {
						IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing));
						if (container == null || WireUtils.getWire(container, location) == null || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing, color);
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[facing.ordinal()] > 0) {
							propagateNotifyCorner(location.facing, facing, color);
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				if (neighborLevel[nLoc.ordinal()] < newSignal - 1 || neighborLevel[nLoc.ordinal()] > (newSignal + 1)) {
					if (connectsInternal(nLoc)) {
						WireUtils.getWire(getContainer(), nLoc).onSignalChanged(color);
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing;

						if (connectsExternal(facing)) {
							propagateNotify(facing, color);
						} else if (connectsCorner(facing)) {
							propagateNotifyCorner(location.facing, facing, color);
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
			System.out.println("--- B! PROPAGATE " + getPos().toString() + " " + location.name() + " --- " + color);
			System.out.println("ConnectionCache: " + Integer.toBinaryString(internalConnections) + " " + Integer.toBinaryString(externalConnections) + " " + Integer.toBinaryString(cornerConnections));
		}

		byte[][] nValues = new byte[6][];

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (connectsExternal(facing)) {
				BlockPos posFacing = getPos().offset(facing);
				IBundledEmitter emitter = null;

				IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), posFacing);
				if (container != null) {
					if (container.hasCapability(Capabilities.BUNDLED_EMITTER, WireUtils.getSlotForFace(location), facing.getOpposite())) {
						emitter = container.getCapability(Capabilities.BUNDLED_EMITTER, WireUtils.getSlotForFace(location), facing.getOpposite());
					}
				} else {
					TileEntity tile = getWorld().getTileEntity(posFacing);
					if (tile.hasCapability(Capabilities.BUNDLED_EMITTER, facing.getOpposite())) {
						emitter = tile.getCapability(Capabilities.BUNDLED_EMITTER, facing.getOpposite());
					}
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
		if (getWorld() != null && getPos() != null && !getWorld().isRemote) {
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
	public boolean hasCapability(Capability<?> capability, PartSlot partSlot, EnumFacing face) {
		if (capability == Capabilities.BUNDLED_RECEIVER) {
			return partSlot == WireUtils.getSlotForFace(location) ? connects(face) : false;
		}
		if (capability == Capabilities.BUNDLED_EMITTER) {
			return partSlot == WireUtils.getSlotForFace(location) ? connects(face) : false;
		}
		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, PartSlot partSlot, EnumFacing face) {
		if (!hasCapability(capability, partSlot, face)) {
			return null;
		}
		if (capability == Capabilities.BUNDLED_RECEIVER) {
			return (T) this;
		}
		if (capability == Capabilities.BUNDLED_EMITTER) {
			return (T) this;
		}
		return null;
	}
}
