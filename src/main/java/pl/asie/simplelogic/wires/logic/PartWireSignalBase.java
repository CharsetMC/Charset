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

import java.util.*;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RedstoneUtils;
import pl.asie.charset.lib.wires.*;
import pl.asie.simplelogic.wires.OldWireUtils;

import javax.annotation.Nonnull;

public abstract class PartWireSignalBase extends Wire implements IWire {
	public static boolean DEBUG = true;
	public static boolean PROPAGATING = false;
	public static boolean WIRES_CONNECT_REDSTONE = true;
	private final EnumSet<EnumFacing> propagationDirs = EnumSet.noneOf(EnumFacing.class);
	private boolean logicUpdateNeeded = true;
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

	protected WireSignalFactory getSignalFactory() {
		return (WireSignalFactory) getProvider();
	}

	protected abstract void onSignalChanged(int color);

	@Override
	public void update() {
		super.update();

		if (logicUpdateNeeded) {
			if (!getContainer().world().isRemote) {
				onSignalChanged(-1);
			}
			logicUpdateNeeded = false;
		}
	}

	protected void scheduleLogicUpdate() {
		logicUpdateNeeded = true;
	}

	@Override
	public String getDisplayName() {
		String name = "";

		switch (getWireType()) {
			case NORMAL:
				name = I18n.translateToLocal("tile.simplelogic.wire" + (getLocation() == WireFace.CENTER ? ".freestanding.name" : ".name"));
				break;
			case INSULATED:
				name = String.format(I18n.translateToLocal("tile.simplelogic.wire.insulated" + (getLocation() == WireFace.CENTER ? ".freestanding.name" : ".name")),
						I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", EnumDyeColor.byMetadata(color))));
				break;
			case BUNDLED:
				name = I18n.translateToLocal("tile.simplelogic.wire.bundled" + (getLocation() == WireFace.CENTER ? ".freestanding.name" : ".name"));
				break;
		}

		return name;
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
				return sWire.getWireType() != WireType.INSULATED;
			case BUNDLED:
				return sWire.getWireType() != WireType.NORMAL;
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

	public abstract void propagate(int color);

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
	protected final void updateConnections() {
		for (int j = 0; j < 6; j++) {
			//if ((i & (1 << j)) != 0) {
				EnumFacing facing = EnumFacing.getFront(j);
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
			//}
		}

		super.updateConnections();

		scheduleLogicUpdate();
	}

	protected void propagateNotifyCorner(EnumFacing side, EnumFacing direction, int color) {
		Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos().offset(side).offset(direction), WireFace.get(direction.getOpposite()));
		if (wire != null && wire instanceof PartWireSignalBase) {
			((PartWireSignalBase) wire).onSignalChanged(color);
		}
	}

	protected void propagateNotify(EnumFacing facing, int color) {
		Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos().offset(facing), getLocation());
		if (wire != null && wire instanceof PartWireSignalBase) {
			((PartWireSignalBase) wire).onSignalChanged(color);
		} else {
			propagationDirs.add(facing);
		}
	}

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
	}

	protected final void neighborChanged(BlockPos neighborPos) {
		getContainer().world().neighborChanged(neighborPos, CharsetLibWires.blockWire, getContainer().pos());
	}

	public int getBundledSignalLevel(int i) {
		return 0;
	}

	@Override
	public boolean canConnectRedstone(EnumFacing facing) {
		return WIRES_CONNECT_REDSTONE && getSignalFactory().type != WireType.BUNDLED && connectsExternal(facing);
	}

	@Override
	public int getWeakPower(EnumFacing facing) {
		if (!PROPAGATING && connectsWeak(facing)) {
			return getRedstoneLevel();
		} else {
			return 0;
		}
	}

	public boolean connectsWeak(EnumFacing facing) {
		if (getSignalFactory().type == WireType.BUNDLED) {
			return false;
		}

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
		if (getSignalFactory().type == WireType.NORMAL && getLocation().facing == facing) {
			return getRedstoneLevel();
		} else {
			return 0;
		}
	}

	@Override
	public boolean renderEquals(Wire other) {
		return super.renderEquals(other)
				&& (getWireType() == WireType.INSULATED || ((PartWireSignalBase) other).getRedstoneLevel() == getRedstoneLevel());
	}

	@Override
	public int renderHashCode() {
		return Objects.hash(super.renderHashCode(), getWireType() == WireType.INSULATED ? 0 : getRedstoneLevel());
	}
}
