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

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.wires.*;
import pl.asie.simplelogic.wires.OldWireUtils;

import javax.annotation.Nonnull;

public class PartWireNormal extends PartWireSignalBase implements IRedstoneEmitter, IRedstoneReceiver {
	private int signalLevel;

	public PartWireNormal(@Nonnull IWireContainer container, @Nonnull WireProvider factory, @Nonnull WireFace location) {
		super(container, factory, location);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		if (getWireType() == WireType.INSULATED) {
			int c = 0xFF000000 | EnumDyeColor.byMetadata(getColor()).getColorValue();
			return (c & 0xFF00FF00) | ((c >> 16) & 0xFF) | ((c << 16) & 0xFF0000);
		} else {
			int signalValue = signalLevel >> 8;
			int v = (signalValue > 0 ? 0x96 : 0x78) + (signalValue * 7);
			return 0xFF000000 | (v << 16) | (v << 8) | v;
		}
	}

	@Override
	public void readNBTData(NBTTagCompound nbt, boolean isClient) {
		super.readNBTData(nbt, isClient);
		if (nbt.hasKey("s")) {
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

	protected int getRedstoneLevel(IBlockAccess world, BlockPos pos, WireFace location) {
		Wire wire = WireUtils.getWire(world, pos, location);
		return wire instanceof PartWireSignalBase ? ((PartWireSignalBase) wire).getSignalLevel() : 0;
	}

	@Override
	public void propagate(int color) {
		if (DEBUG) {
			System.out.println("--- PROPAGATE " + getContainer().pos().toString() + " " + getLocation().name() + " (" + getContainer().world().getTotalWorldTime() + ") ---");
		}


		int maxSignal = 0;
		int oldSignal = signalLevel;
		int[] neighborLevel = new int[7];
		boolean[] isWire = new boolean[7];
		boolean hasRedstoneWire = false;

		PartWireSignalBase.PROPAGATING = true;

		if (getWireType() == WireType.NORMAL) {
			if (getLocation() != WireFace.CENTER) {
				EnumFacing facing = getLocation().facing;

				BlockPos pos = getContainer().pos().offset(facing);
				IBlockState state = getContainer().world().getBlockState(pos);

				int power = OldWireUtils.getRedstoneLevel(this, pos, state, facing, getLocation(), false);

				if (power > 0) {
					neighborLevel[facing.ordinal()] = Math.min(15, power) << 8 | 0xFF;
				}
			}
		}

		for (WireFace location : WireFace.VALUES) {
			if (connectsInternal(location)) {
				isWire[location.ordinal()] = true;
				neighborLevel[location.ordinal()] = getRedstoneLevel(getContainer().world(), getContainer().pos(), location);
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			int facidx = facing.ordinal();

			if (facing == getLocation().facing && getWireType() == WireType.NORMAL) {
				BlockPos pos = getContainer().pos().offset(facing);
				int i = 0;

				for (EnumFacing enumfacing : EnumFacing.values()) {
					if (enumfacing == facing.getOpposite()) {
						continue;
					}

					IBlockState state = getContainer().world().getBlockState(pos.offset(enumfacing));
					Block block = state.getBlock();

					if (!(block instanceof BlockRedstoneWire)) {
						int power = OldWireUtils.getRedstoneLevel(this, pos.offset(enumfacing), state, enumfacing, getLocation(), true);

						if (power >= 15) {
							i = 15;
							break;
						}

						if (power > i) {
							i = power;
						}
					}
				}

				if (i > 0) {
					neighborLevel[facidx] = (i << 8) | 0xFF;
				}
			} else if (connectsExternal(facing)) {
				BlockPos pos = getContainer().pos().offset(facing);
				Wire wire = WireUtils.getWire(getContainer().world(), pos, getLocation());

				if (wire instanceof PartWireSignalBase) {
					isWire[facidx] = true;
					neighborLevel[facidx] = getRedstoneLevel(getContainer().world(), pos, getLocation());
				} else {
					IBlockState state = getContainer().world().getBlockState(pos);

					int power = OldWireUtils.getRedstoneLevel(this, pos, state, facing, getLocation(), true);

					if (state.getBlock() instanceof BlockRedstoneWire) {
						isWire[facidx] = true;
						hasRedstoneWire = true;
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
					neighborLevel[facidx] = getRedstoneLevel(getContainer().world(), pos, WireFace.get(facing.getOpposite()));
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
// TODO			System.out.println("ConnectionCache: " + Integer.toBinaryString(internalConnections) + " " + Integer.toBinaryString(externalConnections) + " " + Integer.toBinaryString(cornerConnections));
			System.out.println("Levels: " + Arrays.toString(neighborLevel));
		}

		if (maxSignal > signalLevel) {
			signalLevel = maxSignal - 1;
			if ((signalLevel & 0xFF) == 0 || (signalLevel & 0xFF) == 0xFF) {
				signalLevel = 0;
			}
		} else {
			signalLevel = maxSignalNonWire;
		}

		if (oldSignal == signalLevel) {
			return;
		}

		if (DEBUG) {
			System.out.println("Switch: " + oldSignal + " -> " + signalLevel);
		}

		if (signalLevel == 0) {
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
					} else if (getWireType() == WireType.NORMAL && facing.getOpposite() != getLocation().facing) {
						TileEntity nt = getContainer().world().getTileEntity(getContainer().pos().offset(facing));
						if (!(nt instanceof IRedstoneReceiver)) {
							neighborChanged(getContainer().pos().offset(facing));
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				if (neighborLevel[nLoc.ordinal()] < signalLevel - 1 || neighborLevel[nLoc.ordinal()] > (signalLevel + 1)) {
					if (connectsInternal(nLoc)) {
						Wire wire = WireUtils.getWire(getContainer().world(), getContainer().pos(), nLoc);
						if (wire instanceof PartWireSignalBase) ((PartWireSignalBase) wire).onSignalChanged(getColor());
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing;

						if (connectsExternal(facing)) {
							propagateNotify(facing, getColor());
						} else if (connectsCorner(facing)) {
							propagateNotifyCorner(getLocation().facing, facing, getColor());
						} else if (getWireType() == WireType.NORMAL && facing.getOpposite() != getLocation().facing) {
							TileEntity nt = getContainer().world().getTileEntity(getContainer().pos().offset(facing));
							if (!(nt instanceof IRedstoneReceiver)) {
								neighborChanged(getContainer().pos().offset(facing));
							}
						}
					}
				}
			}
		}

		if (getWireType() == WireType.NORMAL) {
			if ((oldSignal & 0xF00) != (signalLevel & 0xF00)) {
				getContainer().requestRenderUpdate();

				if (getLocation() != WireFace.CENTER) {
					neighborChanged(getContainer().pos().offset(getLocation().facing));
				}
			}
		} else {
			if ((oldSignal & 0xF00) != (signalLevel & 0xF00)) {
				if (getLocation() != WireFace.CENTER) {
					neighborChanged(getContainer().pos().offset(getLocation().facing));
				}
			}
		}

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
			return (T) this;
		}
		if (capability == Capabilities.REDSTONE_EMITTER) {
			return (T) this;
		}
		return super.getCapability(capability, enumFacing);
	}

	@Override
	public int getRedstoneSignal() {
		return getRedstoneLevel();
	}

	@Override
	public void onRedstoneInputChange() {
		scheduleLogicUpdate();
	}
}
