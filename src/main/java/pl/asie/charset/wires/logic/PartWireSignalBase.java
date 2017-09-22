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

import java.util.*;

import io.netty.buffer.ByteBuf;

import mcmultipart.multipart.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import mcmultipart.MCMultiPartMod;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.MultipartUtils;
import pl.asie.charset.lib.utils.RedstoneUtils;
import pl.asie.charset.lib.wires.PartWire;
import pl.asie.charset.wires.WireUtils;

public abstract class PartWireSignalBase extends PartWire implements
		IRedstonePart.ISlottedRedstonePart, IWire {
	public static boolean DEBUG = false;
	public static boolean PROPAGATING = false;
	public static boolean WIRES_CONNECT_REDSTONE = true;
	private final EnumSet<EnumFacing> propagationDirs = EnumSet.noneOf(EnumFacing.class);
	private int color = -1;

	public PartWireSignalBase() {
		scheduleConnectionUpdate();
	}

	public int getColor() {
		return color;
	}

	protected void setColor(int color) {
		this.color = color;
	}

	protected WireSignalFactory getSignalFactory() {
		return (WireSignalFactory) getFactory();
	}

	protected abstract void onSignalChanged(int color);

	@Override
	protected void logicUpdate() {
		if (!getWorld().isRemote) {
			onSignalChanged(-1);
		}
	}

	@Override
	public String getDisplayName() {
		String name = "";

		switch (getWireType()) {
			case NORMAL:
				name = I18n.translateToLocal("tile.charset.wire.name");
				break;
			case INSULATED:
				name = String.format(I18n.translateToLocal("tile.charset.wire.insulated.name"), I18n.translateToLocal(ColorUtils.getLangEntry("charset.color.", color)));
				break;
			case BUNDLED:
				name = I18n.translateToLocal("tile.charset.wire.bundled.name");
				break;
		}

		if (location == WireFace.CENTER) {
			name = String.format(I18n.translateToLocal("tile.charset.wire.freestanding.name"), name);
		}

		return name;
	}

	@Override
	public boolean calculateConnectionWire(PartWire wire) {
		if (super.calculateConnectionWire(wire)) {
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
	public boolean calculateConnectionNonWire(BlockPos pos, EnumFacing direction) {
		if (getSignalFactory().type == WireType.BUNDLED) {
			if (MultipartUtils.hasCapability(Capabilities.BUNDLED_EMITTER, getWorld(), pos, WireUtils.getSlotForFace(location), direction)) {
				return true;
			}

			if (MultipartUtils.hasCapability(Capabilities.BUNDLED_RECEIVER, getWorld(), pos, WireUtils.getSlotForFace(location), direction)) {
				return true;
			}
		} else {
			if (MultipartUtils.hasCapability(Capabilities.REDSTONE_EMITTER, getWorld(), pos, WireUtils.getSlotForFace(location), direction)) {
				return true;
			}

			if (MultipartUtils.hasCapability(Capabilities.REDSTONE_RECEIVER, getWorld(), pos, WireUtils.getSlotForFace(location), direction)) {
				return true;
			}

			IBlockState connectingState = getWorld().getBlockState(pos);
			Block connectingBlock = connectingState.getBlock();

			if (location == WireFace.CENTER && !connectingBlock.isSideSolid(connectingState, getWorld(), pos, direction)) {
				return false;
			}

			WIRES_CONNECT_REDSTONE = false;
			boolean connectRS = RedstoneUtils.canConnectFace(getWorld(), pos, connectingState, direction.getOpposite(), location.facing);
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
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		buf.writeByte(color);
	}

	public void handlePacket(ByteBuf buf) {
		super.handlePacket(buf);
		color = buf.readByte();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (nbt.hasKey("col")) {
			color = nbt.getByte("col");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (color != -1) {
			nbt.setByte("col", (byte) color);
		}
		return super.writeToNBT(nbt);
	}

	protected void neighborUpdate(int i) {
		for (int j = 0; j < 6; j++) {
			if ((i & (1 << j)) != 0) {
				EnumFacing facing = EnumFacing.getFront(j);
				TileEntity tile = getWorld().getTileEntity(getPos().offset(facing));
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
		}

		super.neighborUpdate(i);
	}

	protected void propagateNotifyCorner(EnumFacing side, EnumFacing direction, int color) {
		PartWireSignalBase wire = WireUtils.getWire(MultipartHelper.getPartContainer(getWorld(), getPos().offset(side).offset(direction)), WireFace.get(direction.getOpposite()));
		if (wire != null) {
			wire.onSignalChanged(color);
		}
	}

	protected void propagateNotify(EnumFacing facing, int color) {
		PartWireSignalBase wire = WireUtils.getWire(MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing)), location);
		if (wire != null) {
			wire.onSignalChanged(color);
		} else {
			propagationDirs.add(facing);
		}
	}

	protected void finishPropagation() {
		for (EnumFacing facing : propagationDirs) {
			TileEntity nt = getWorld().getTileEntity(getPos().offset(facing));
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
				getWorld().notifyBlockOfStateChange(getPos().offset(facing), MCMultiPartMod.multipart);
			}
		}

		propagationDirs.clear();
	}

	public int getBundledSignalLevel(int i) {
		return 0;
	}

	@Override
	public boolean canConnectRedstone(EnumFacing facing) {
		return WIRES_CONNECT_REDSTONE && getSignalFactory().type != WireType.BUNDLED && connectsExternal(facing);
	}

	@Override
	public int getWeakSignal(EnumFacing facing) {
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
		if (location.facing == facing) {
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
	public int getStrongSignal(EnumFacing facing) {
		if (getSignalFactory().type == WireType.NORMAL && location.facing == facing) {
			return getRedstoneLevel();
		} else {
			return 0;
		}
	}

	@Override
	public boolean renderEquals(PartWire other) {
		return super.renderEquals(other)
				&& (getWireType() == WireType.INSULATED || ((PartWireSignalBase) other).getRedstoneLevel() == getRedstoneLevel());
	}

	@Override
	public int renderHashCode() {
		return Objects.hash(super.renderHashCode(), getWireType() == WireType.INSULATED ? 0 : getRedstoneLevel());
	}
}
