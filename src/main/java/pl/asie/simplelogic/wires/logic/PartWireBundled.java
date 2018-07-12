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
import java.util.List;
import java.util.Objects;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.*;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;
import pl.asie.charset.lib.wires.WireUtils;
import pl.asie.simplelogic.wires.LogicWireUtils;

import javax.annotation.Nonnull;

public class PartWireBundled extends PartWireSignalBase implements IBundledReceiver, IBundledEmitter {
	private int[] insulatedColorCache = null;
	private int[] signalLevel = new int[16];
	private byte[] signalValue = new byte[16];

	public PartWireBundled(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	protected int[] getInsulatedColorCache() {
		if (getContainer().world() != null && getContainer().pos() != null) {
			if (insulatedColorCache == null) {
				insulatedColorCache = new int[6];

				for (EnumFacing facing : EnumFacing.VALUES) {
					BlockPos pos = getContainer().pos().offset(facing);
					WireFace face = getLocation();

					if (connectsCorner(facing)) {
						pos = pos.offset(face.facing);
						face = WireFace.get(facing.getOpposite());
					} else if (!connectsExternal(facing)) {
						insulatedColorCache[facing.ordinal()] = -1;
						continue;
					}

					Wire targetWire = WireUtils.getWire(getContainer().world(), pos, face);
					if (targetWire instanceof PartWireInsulated) {
						insulatedColorCache[facing.ordinal()] = ((PartWireInsulated) targetWire).getWireColor();
					} else {
						insulatedColorCache[facing.ordinal()] = -1;
					}
				}
			}
		}

		return insulatedColorCache;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		return -1;
	}

	@Override
	public boolean renderEquals(Wire other) {
		return super.renderEquals(other) && Arrays.equals(((PartWireBundled) other).getInsulatedColorCache(), getInsulatedColorCache());
	}

	@Override
	public int renderHashCode() {
		return Objects.hash(super.renderHashCode(), getInsulatedColorCache());
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		super.readNBTData(nbt, isClient);

		if (nbt.hasKey("s", Constants.NBT.TAG_INT_ARRAY)) {
			signalLevel = nbt.getIntArray("s");
			if (signalLevel.length != 16) {
				signalLevel = new int[16];
			}
		} else {
			signalLevel = new int[16];
		}

		for (int i = 0; i < 16; i++) {
			signalValue[i] = (byte) (signalLevel[i] >> 8);
		}

		insulatedColorCache = null;
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
		super.writeNBTData(nbt, isClient);
		nbt.setIntArray("s", signalLevel);
		return nbt;
	}

	private void propagate(int color, byte[][] nValues) {
		boolean[] isWire = new boolean[7];
		int[] neighborLevel = new int[7];

		int maxSignal = 0;
		int oldSignal = signalLevel[color];

		PartWireSignalBase.PROPAGATING = true;

		for (WireFace location : WireFace.VALUES) {
			if (connectsInternal(location)) {
				isWire[location.ordinal()] = true;
				neighborLevel[location.ordinal()] = LogicWireUtils.getBundledWireLevel(getContainer().world(), getContainer().pos(), location, color);
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (connectsExternal(facing)) {
				if (nValues[facing.ordinal()] != null) {
					int nv = nValues[facing.ordinal()][color];
					// clamp nv
					if (nv < 0 || nv > 0x0F) nv = 0x0F;

					int v = nv << 8;
					if (v != 0) {
						neighborLevel[facing.ordinal()] = v | 0xFF;
					}
				} else {
					BlockPos pos = getContainer().pos().offset(facing);
					Wire wire = WireUtils.getWire(getContainer().world(), pos, getLocation());

					if (wire instanceof PartWireSignalBase) {
						isWire[facing.ordinal()] = true;
						neighborLevel[facing.ordinal()] = LogicWireUtils.getBundledWireLevel(getContainer().world(), pos, getLocation(), color);
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = getContainer().pos().offset(facing).offset(getLocation().facing);
				Wire wire = WireUtils.getWire(getContainer().world(), cornerPos, getLocation());

				if (wire instanceof PartWireSignalBase) {
					isWire[facing.ordinal()] = true;
					neighborLevel[facing.ordinal()] = LogicWireUtils.getBundledWireLevel(getContainer().world(), cornerPos, WireFace.get(facing.getOpposite()), color);
				}
			}
		}

		PartWireSignalBase.PROPAGATING = false;

		int maxSignalNonWire = 0;

		for (int j = 0; j < 7; j++) {
			if (neighborLevel[j] > maxSignal) {
				maxSignal = neighborLevel[j];
			}
			if (!isWire[j] && neighborLevel[j] > maxSignalNonWire) {
				maxSignalNonWire = neighborLevel[j];
			}
		}

		if (DEBUG) {
			System.out.println("[" + color + "] Levels: " + Arrays.toString(neighborLevel));
		}

		int newSignal;
		if (maxSignal > oldSignal) {
			newSignal = maxSignal - 1;
			if ((newSignal & 0xFF) == 0 || (newSignal & 0xFF) == 0xFF) {
				newSignal = 0;
			}
		} else {
			newSignal = maxSignalNonWire;
		}

		// If the signal level did not change, we don't need to update.
		if (oldSignal == newSignal) {
			return;
		}

		signalLevel[color] = newSignal;
		signalValue[color] = (byte) (newSignal >> 8);

		if (newSignal == 0) {
			// If we lost signal, propagate only to those which have a signal.
			// This is an optimization.
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
				IBundledEmitter emitter = null;

				BlockPos pos = getContainer().pos().offset(facing);
				if (WireUtils.hasCapability(this, pos, Capabilities.BUNDLED_EMITTER, facing.getOpposite(), false)) {
					emitter = WireUtils.getCapability(this, pos, Capabilities.BUNDLED_EMITTER, facing.getOpposite(), false);
				}

				if (emitter != null && !(emitter instanceof PartWireSignalBase)) {
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
		return super.hasCapability(capability, face);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing face) {
		if (capability == Capabilities.BUNDLED_RECEIVER) {
			return Capabilities.BUNDLED_RECEIVER.cast(this);
		}
		if (capability == Capabilities.BUNDLED_EMITTER) {
			return Capabilities.BUNDLED_EMITTER.cast(this);
		}
		return super.getCapability(capability, face);
	}

	@Override
	public String getDisplayName() {
		return I18n.translateToLocal("tile.simplelogic.wire.bundled" + (getLocation() == WireFace.CENTER ? ".freestanding.name" : ".name"));
	}

	@Override
	public void addDebugInformation(List<String> stringList, Side side) {
		if (side == Side.SERVER) {
			StringBuilder builder = new StringBuilder(getLocation().name());
			builder.append(' ');
			for (int i = 0; i < 16; i++) {
				builder.append(ColorUtils.getNearestTextFormatting(EnumDyeColor.byMetadata(i)));
				builder.append(signalValue[i] <= 0 ? '_' : Integer.toHexString(signalValue[i]).toUpperCase());
			}
			stringList.add(builder.toString());
		}
	}
}
