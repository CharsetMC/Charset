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

package pl.asie.simplelogic.wires.logic;

import java.util.*;

import mcmultipart.api.multipart.MultipartHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.tools.IStopwatchTracker;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.misc.ISimpleLogicSidedEmitter;
import pl.asie.charset.lib.scheduler.Scheduler;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataProvider;
import pl.asie.charset.lib.utils.redstone.RedstoneUtils;
import pl.asie.charset.lib.wires.*;
import pl.asie.simplelogic.gates.PartGate;

import javax.annotation.Nonnull;

public abstract class PartWireSignalBase extends Wire implements IWire, ISignalMeterDataProvider, IDebuggable, ISimpleLogicSidedEmitter {
	@SuppressWarnings("PointlessBooleanExpression")
	public static boolean DEBUG_CLIENT_WIRE_STATE = false && ModCharset.INDEV;
	@SuppressWarnings("PointlessBooleanExpression")
	public static boolean DEBUG = false && ModCharset.INDEV;

	public static boolean PROPAGATING = false;
	public static boolean WIRES_CONNECT_REDSTONE = true;
	private final EnumSet<EnumFacing> propagationDirs = EnumSet.noneOf(EnumFacing.class);
	private int color = -1;

	public PartWireSignalBase(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	public int getColor() {
		return color;
	}

	protected void setColor(int color) {
		this.color = color;
	}

	protected LogicWireProvider getSignalFactory() {
		return (LogicWireProvider) getProvider();
	}

	protected abstract void onSignalChanged(int color, boolean clearMode);

	protected void scheduleLogicUpdate() {
		if (getContainer().world() != null && getContainer().pos() != null) {
			if (!getContainer().world().isRemote) {
				onSignalChanged(-1, false);
			}
		} else if (getContainer().world() != null) {
			Scheduler.INSTANCE.in(getContainer().world(), 0, this::scheduleLogicUpdate);
		}
	}

	@Override
	public void onChanged(boolean external) {
		super.onChanged(external);
		scheduleLogicUpdate();
	}

	@Override
	protected boolean canConnectWire(Wire wire) {
		if (super.canConnectWire(wire)) {
			return true;
		}

		if (!(wire instanceof PartWireSignalBase)) {
			return false;
		}

		PartWireSignalBase sWire = (PartWireSignalBase) wire;

		switch (getSignalFactory().type) {
			case NORMAL:
				return sWire.getWireType() != WireType.BUNDLED;
			case INSULATED:
				// This will remove insulated wires of OTHER colors.
				// Insulated wires of the same color are handled by the super call above.
				return sWire.getWireType() != WireType.INSULATED;
			case BUNDLED:
				if (sWire.getWireType() == WireType.BUNDLED) {
					return sWire.getColor() == -1 || getColor() == -1;
				} else {
					return sWire.getWireType() != WireType.NORMAL;
				}
		}

		return false;
	}

	@Override
	protected boolean canConnectBlock(BlockPos pos, EnumFacing direction) {
		if (getSignalFactory().type == WireType.BUNDLED) {
			if (WireUtils.hasCapability(this, pos, Capabilities.BUNDLED_EMITTER, direction, true)) {
				return true;
			}

			if (WireUtils.hasCapability(this, pos, Capabilities.BUNDLED_RECEIVER, direction, true)) {
				return true;
			}
		} else {
			if (WireUtils.hasCapability(this, pos, Capabilities.REDSTONE_EMITTER, direction, true)) {
				return true;
			}

			if (WireUtils.hasCapability(this, pos, Capabilities.REDSTONE_RECEIVER, direction, true)) {
				return true;
			}

			IBlockState connectingState = getContainer().world().getBlockState(pos);
			Block connectingBlock = connectingState.getBlock();

			if (getLocation() == WireFace.CENTER && !connectingBlock.isSideSolid(connectingState, getContainer().world(), pos, direction)) {
				return false;
			}

			WIRES_CONNECT_REDSTONE = false;
			boolean connectRS = RedstoneUtils.canConnectFace(getContainer().world(), pos, connectingState, direction.getOpposite(), getLocation().facing);
			WIRES_CONNECT_REDSTONE = true;

			if (connectRS) {
				return true;
			}
		}

		return false;
	}

	public abstract void propagate(int color, PropagationQueue queue);

	public abstract int getSignalLevel();

	public abstract int getRedstoneLevel();

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		super.readNBTData(nbt, isClient);
		if (nbt.hasKey("col")) {
			color = nbt.getByte("col");
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
		if (color != -1) {
			nbt.setByte("col", (byte) color);
		}
		return super.writeNBTData(nbt, isClient);
	}

	@Override
	protected void updateConnections() {
		for (int j = 0; j < 6; j++) {
			EnumFacing facing = EnumFacing.byIndex(j);
			TileEntity tile = getContainer().world().getTileEntity(getContainer().pos().offset(facing));
			if (tile != null) {
				if (getSignalFactory().type == WireType.BUNDLED) {
					if (tile.hasCapability(Capabilities.BUNDLED_RECEIVER, facing.getOpposite())) {
						tile.getCapability(Capabilities.BUNDLED_RECEIVER, facing.getOpposite()).onBundledInputChange();
					}
				} else {
					if (tile.hasCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite())) {
						tile.getCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite()).onRedstoneInputChange();
					}
				}
			}
		}

		super.updateConnections();

		scheduleLogicUpdate();
	}

	protected void propagateNotifyCorner(EnumFacing side, EnumFacing direction, PropagationQueue queue, int color) {
		Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos().offset(side).offset(direction), WireFace.get(direction.getOpposite()));
		if (wire instanceof PartWireSignalBase) {
			queue.add((PartWireSignalBase) wire, color);
		}
	}

	protected void propagateNotify(EnumFacing facing, PropagationQueue queue, int color) {
		Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos().offset(facing), getLocation());
		if (wire instanceof PartWireSignalBase) {
			queue.add((PartWireSignalBase) wire, color);
		} else {
			propagationDirs.add(facing);
		}
	}

	// TODO: hack
	private boolean scheduledStopwatch;

	protected void finishPropagation() {
		for (EnumFacing facing : propagationDirs) {
			TileEntity nt = getContainer().world().getTileEntity(getContainer().pos().offset(facing));
			boolean found = false;
			if (nt != null) {
				if (getSignalFactory().type == WireType.BUNDLED) {
					if (nt.hasCapability(Capabilities.BUNDLED_RECEIVER, facing.getOpposite())) {
						nt.getCapability(Capabilities.BUNDLED_RECEIVER, facing.getOpposite()).onBundledInputChange();
						found = true;
					}
				} else {
					if (nt.hasCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite())) {
						nt.getCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite()).onRedstoneInputChange();
						found = true;
					}
				}
			}

			if (getSignalFactory().type != WireType.BUNDLED && !found) {
				neighborChanged(getContainer().pos().offset(facing));
			}
		}

		propagationDirs.clear();

		if (!scheduledStopwatch) {
			scheduledStopwatch = true;
			if (getContainer().world().hasCapability(Capabilities.STOPWATCH_TRACKER, null)) {
				IStopwatchTracker tracker = getContainer().world().getCapability(Capabilities.STOPWATCH_TRACKER, null);
				if (tracker != null) {
					Scheduler.INSTANCE.in(MultipartHelper.unwrapWorld(getContainer().world()), 0, () -> {
						tracker.markChanged(getContainer().pos());
						scheduledStopwatch = false;
					});
				}
			}
		}
	}

	protected final void neighborChanged(BlockPos neighborPos) {
		getContainer().world().neighborChanged(neighborPos, CharsetLibWires.blockWire, getContainer().pos());
	}

	public int getBundledSignalLevel(int i) {
		return 0;
	}

	@Override
	public boolean canConnectRedstone(EnumFacing facing) {
		return WIRES_CONNECT_REDSTONE && connectsExternal(facing);
	}

	@Override
	public int getWeakPower(EnumFacing facing) {
		if (!PROPAGATING && facing != null && connectsWeak(facing.getOpposite())) {
			if (facing == EnumFacing.UP) {
				IBlockState state = getContainer().world().getBlockState(getContainer().pos().down());
				if (state.getBlock() instanceof BlockRedstoneWire) {
					return 0;
				}
			}

			return getRedstoneLevel();
		} else {
			return 0;
		}
	}

	public boolean connectsWeak(EnumFacing facing) {
		// Block any signals if there's a wire on the target face
		if (getLocation().facing == facing) {
			return true;
		} else {
			if (connects(facing) || getSignalFactory().type == WireType.NORMAL) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public WireType getWireType() {
		return getSignalFactory().type;
	}

	@Override
	public int getStrongPower(EnumFacing facing) {
		return 0;
	}

	public EnumFacing getEmitterFace() {
		return getLocation().facing;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == Capabilities.DEBUGGABLE || capability == Capabilities.SIGNAL_METER_DATA_PROVIDER) {
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == Capabilities.DEBUGGABLE || capability == Capabilities.SIGNAL_METER_DATA_PROVIDER) {
			//noinspection unchecked
			return (T) this;
		}

		return super.getCapability(capability, facing);
	}
}
