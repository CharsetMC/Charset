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

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;
import pl.asie.charset.lib.wires.*;
import pl.asie.simplelogic.wires.LogicWireUtils;
import pl.asie.simplelogic.wires.SimpleLogicWires;

import javax.annotation.Nonnull;

public class PartWireNormal extends PartWireSignalBase implements IRedstoneEmitter, IRedstoneReceiver {
	private static final ResourceLocation REDSTONE_PASTE_BLOCK = new ResourceLocation("redstonepaste:redstonepaste");

	private int signalLevel;

	public PartWireNormal(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		int signalValue = signalLevel >> 8;
		int v = (signalValue > 0 ? 0x96 : 0x78) + (signalValue * 7);
		return 0xFF000000 | (v * 0x10101);
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		super.readNBTData(nbt, isClient);
		if (nbt.hasKey("s", Constants.NBT.TAG_ANY_NUMERIC)) {
			signalLevel = nbt.getShort("s");
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
		super.writeNBTData(nbt, isClient);
		nbt.setShort("s", (short) signalLevel);
		return nbt;
	}

	@Override
	protected void onSignalChanged(int color) {
		if (getContainer().world() != null && getContainer().pos() != null && !getContainer().world().isRemote) {
			propagate(color);
		}
	}

	protected int getWireRedstoneLevel(IBlockAccess world, BlockPos pos, WireFace location) {
		Wire wire = WireUtils.getWire(world, pos, location);
		return wire instanceof PartWireSignalBase ? ((PartWireSignalBase) wire).getSignalLevel() : 0;
	}

	@Override
	public void propagate(int color) {
		if (DEBUG) {
			System.out.println("--- PROPAGATE " + getContainer().pos().toString() + " " + getLocation().name() + " (" + getContainer().world().getTotalWorldTime() + ") ---");
		}

		boolean[] isWire = new boolean[7];
		int[] neighborLevel = new int[7];

		int maxSignal = 0;
		int oldSignal = signalLevel;

		PartWireSignalBase.PROPAGATING = true;

		// First, get the strength from the full block we're placed on, if any.
		if (getWireType() == WireType.NORMAL) {
			if (getLocation() != WireFace.CENTER) {
				EnumFacing facing = getLocation().facing;

				BlockPos pos = getContainer().pos().offset(facing);
				IBlockState state = getContainer().world().getBlockState(pos);

				// Weak power (on block)
				int power = LogicWireUtils.getWeakRedstoneLevel(this, pos, state, facing, getLocation());

				if (power < 15) {
					// Strong power (on surrounding blocks)
					for (EnumFacing enumfacing : EnumFacing.values()) {
						if (enumfacing == facing.getOpposite()) {
							continue;
						}

						state = getContainer().world().getBlockState(pos.offset(enumfacing));
						Block block = state.getBlock();

						if (!(block instanceof BlockRedstoneWire)) {
							int currPower = LogicWireUtils.getStrongRedstoneLevel(this, pos.offset(enumfacing), state, enumfacing, getLocation());

							if (currPower >= 15) {
								power = 15;
								break;
							} else if (currPower > power) {
								power = currPower;
							}
						}
					}
				}

				if (power > 0) {
					neighborLevel[facing.ordinal()] = (Math.min(power, 15) << 8) | 0xFF;
				}
			}
		}

		// Check for internal connections (wires only)
		for (WireFace location : WireFace.VALUES) {
			if (connectsInternal(location)) {
				isWire[location.ordinal()] = true;
				neighborLevel[location.ordinal()] = getWireRedstoneLevel(getContainer().world(), getContainer().pos(), location);
			}
		}

		// Check for external connections (one block away)
		for (EnumFacing facing : EnumFacing.VALUES) {
			int facidx = facing.ordinal();

			if (connectsExternal(facing)) {
				BlockPos pos = getContainer().pos().offset(facing);
				Wire wire = WireUtils.getWire(getContainer().world(), pos, getLocation());

				// If we have a wire, treat it as a wire. If not, treat it as a block.
				if (wire instanceof PartWireSignalBase) {
					isWire[facidx] = true;
					neighborLevel[facidx] = getWireRedstoneLevel(getContainer().world(), pos, getLocation());
				} else {
					IBlockState state = getContainer().world().getBlockState(pos);
					int power = LogicWireUtils.getWeakRedstoneLevel(this, pos, state, facing, getLocation());

					if (state.getBlock() instanceof BlockRedstoneWire || state.getBlock().getRegistryName().equals(REDSTONE_PASTE_BLOCK)) {
						isWire[facidx] = true;
						power--;
					}

					if (power > 0) {
						neighborLevel[facidx] = (Math.min(power, 15) << 8) | 0xFF;
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos pos = getContainer().pos().offset(facing).offset(getLocation().facing);
				Wire wire = WireUtils.getWire(getContainer().world(), pos, WireFace.get(facing.getOpposite()));
				if (wire instanceof PartWireSignalBase) {
					isWire[facidx] = true;
					neighborLevel[facidx] = getWireRedstoneLevel(getContainer().world(), pos, WireFace.get(facing.getOpposite()));
				}
			}
		}

		PartWireSignalBase.PROPAGATING = false;

		int maxSignalNonWire = 0;

		for (int i = 0; i < 7; i++) {
			if (neighborLevel[i] > maxSignal) {
				maxSignal = neighborLevel[i];
			}
			if (!isWire[i] && neighborLevel[i] > maxSignalNonWire) {
				maxSignalNonWire = neighborLevel[i];
			}
		}

		if (DEBUG) {
			System.out.println("Levels: " + Arrays.toString(neighborLevel));
			System.out.println("IsWire: " + Arrays.toString(isWire));
		}

		if (maxSignal > signalLevel) {
			signalLevel = maxSignal - 1;
			if ((signalLevel & 0xFF) == 0 || (signalLevel & 0xFF) == 0xFF) {
				signalLevel = 0;
			}
		} else {
			signalLevel = maxSignalNonWire;
		}

		// If the signal level did not change, we don't need to update.
		if (oldSignal == signalLevel) {
			return;
		}

		if (DEBUG) {
			System.out.println("Switch: " + oldSignal + " -> " + signalLevel);
		}

		if (signalLevel == 0) {
			// If we lost signal, propagate only to those which have a signal.
			// This is an optimization.
			for (WireFace nLoc : WireFace.VALUES) {
				if (connectsInternal(nLoc)) {
					if (neighborLevel[nLoc.ordinal()] > 0) {
						Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos(), nLoc);
						if (wire instanceof PartWireSignalBase) ((PartWireSignalBase) wire).onSignalChanged(getColor());
					}
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing;

					if (connectsExternal(facing)) {
						Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos().offset(facing), getLocation());
						if (!(wire instanceof PartWireSignalBase) || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing, getColor());
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[nLoc.ordinal()] > 0) {
							propagateNotifyCorner(getLocation().facing, facing, getColor());
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				boolean nChanged = neighborLevel[nLoc.ordinal()] < (signalLevel - 1) || neighborLevel[nLoc.ordinal()] > (signalLevel + 1);
				if (connectsInternal(nLoc)) {
					if (nChanged) {
						Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos(), nLoc);
						if (wire instanceof PartWireSignalBase) ((PartWireSignalBase) wire).onSignalChanged(getColor());
					}
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing;

					if (connectsExternal(facing)) {
						Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos().offset(facing), getLocation());
						if (!(wire instanceof PartWireSignalBase) || nChanged) {
							propagateNotify(facing, getColor());
						}
					} else if (connectsCorner(facing)) {
						if (nChanged) {
							propagateNotifyCorner(getLocation().facing, facing, getColor());
						}
					}
				}
			}
		}

		// TODO: This can probably be optimized a little bit more.
		if (getWireType() == WireType.NORMAL) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (facing != getLocation().facing && (connectsExternal(facing) || connectsCorner(facing))) {
					continue;
				}

				TileEntity nt = getContainer().world().getTileEntity(getContainer().pos().offset(facing));
				if (nt == null || !(nt.hasCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite()))) {
					neighborChanged(getContainer().pos().offset(facing));
				}

				if (facing == getLocation().facing) {
					EnumFacing facingO = facing.getOpposite();
					for (EnumFacing facing2 : EnumFacing.VALUES) {
						if (facing2 != facingO) {
							neighborChanged(getContainer().pos().offset(facing).offset(facing2));
						}
					}
				}
			}
		}

		// Handle rendering updates.
		if ((oldSignal & 0xF00) != (signalLevel & 0xF00)) {
			if (getWireType() == WireType.NORMAL || PartWireSignalBase.DEBUG_CLIENT_WIRE_STATE) {
				if (SimpleLogicWires.useTESRs) {
					getContainer().requestNetworkUpdate();
				} else {
					getContainer().requestRenderUpdate();
				}
			}

			if (getLocation() != WireFace.CENTER) {
				neighborChanged(getContainer().pos().offset(getLocation().facing));
			}
		}

		if (signalLevel == 0) {
			propagate(color);
		}

		// Once we're done propagating, update emitters and receivers.
		finishPropagation();
	}

	@Override
	public int getSignalLevel() {
		return signalLevel;
	}

	@Override
	public int getRedstoneLevel() {
		return signalLevel >> 8;
	}

	/* @Override
	public int getRedstoneSignal(WireFace face, EnumFacing toDirection) {
		if (face == null) {
			return getWeakSignal(toDirection);
		} else {
			return face == location && connects(toDirection) ? getRedstoneLevel() : 0;
		}
	} */

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing face) {
		if (capability == Capabilities.REDSTONE_RECEIVER) {
			return connects(face);
		}
		if (capability == Capabilities.REDSTONE_EMITTER) {
			return connects(face);
		}
		return super.hasCapability(capability, face);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
		if (capability == Capabilities.REDSTONE_RECEIVER) {
			return Capabilities.REDSTONE_RECEIVER.cast(this);
		}
		if (capability == Capabilities.REDSTONE_EMITTER) {
			return Capabilities.REDSTONE_EMITTER.cast(this);
		}
		return super.getCapability(capability, enumFacing);
	}

	@Override
	public String getDisplayName() {
		return I18n.translateToLocal("tile.simplelogic.wire" + (getLocation() == WireFace.CENTER ? ".freestanding.name" : ".name"));
	}

	// IRedstoneEmitter shenanigans, do not use directly

	@Override
	@Deprecated
	public int getRedstoneSignal() {
		return !PROPAGATING ? getRedstoneLevel() : 0;
	}

	@Override
	public int getStrongPower(EnumFacing facing) {
		if (!PROPAGATING) {
			if (getSignalFactory().type == WireType.NORMAL && facing != null && getLocation().facing == facing.getOpposite()) {
				return getRedstoneLevel();
			}
		}

		return 0;
	}

	@Override
	public void onRedstoneInputChange() {
		scheduleLogicUpdate();
	}

	@Override
	public void addDebugInformation(List<String> stringList, Side side) {
		if (side == Side.CLIENT && !PartWireSignalBase.DEBUG_CLIENT_WIRE_STATE) {
			return;
		}

		stringList.add(getLocation().name() + " R:" + (signalLevel >> 8) + " S:" + (signalLevel & 0xFF));
	}

	@Override
	public boolean renderEquals(Wire other) {
		if (getWireType() != WireType.NORMAL) {
			return super.renderEquals(other);
		}

		return super.renderEquals(other) && (((PartWireSignalBase) other).getRedstoneLevel() == getRedstoneLevel());
	}

	@Override
	public int renderHashCode() {
		if (getWireType() != WireType.NORMAL) {
			return super.renderHashCode();
		}

		return Objects.hash(super.renderHashCode(), getRedstoneLevel());
	}

	@Override
	public ISignalMeterData getSignalMeterData(RayTraceResult result) {
		return new SignalMeterDataWire((byte) getRedstoneLevel(), getColor());
	}
}
