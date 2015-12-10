package pl.asie.charset.wires.logic;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.wires.BlockWire;
import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireKind;

public class WireNormal extends Wire {
	private int signalLevel;
	private byte signalValue;

	public WireNormal(WireKind type, WireFace location, TileWireContainer container) {
		super(type, location, container);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		if (type.type() == WireType.INSULATED) {
			return EnumDyeColor.byMetadata(type.color()).getMapColor().colorValue;
		} else {
			int v = (signalValue > 0 ? 0x96 : 0x78) + (signalValue * 7);
			return (v << 16) | (v << 8) | v;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		signalLevel = nbt.getShort("s");
		signalValue = nbt.getByte("v");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setShort("s", (short) signalLevel);
		nbt.setByte("v", signalValue);
	}

	protected int getSignalLevel(TileWireContainer container, WireFace location) {
		return container.getSignalLevel(location);
	}

	protected byte getRedstoneLevel(TileWireContainer container, WireFace location) {
		return (byte) container.getRedstoneLevel(location);
	}

	@Override
	public void propagate() {
		if (DEBUG) {
			System.out.println("--- PROPAGATE " + container.getPos().toString() + " " + location.name() + " ---");
		}

		int maxSignal = 0;
		byte maxValue = 0;
		int oldSignal = signalLevel;
		byte oldValue = signalValue;
		int[] neighborLevel = new int[7];
		byte[] neighborValue = new byte[7];
		int neighbor = 0;

		if (internalConnections > 0) {
			for (WireFace location : WireFace.VALUES) {
				if (connectsInternal(location)) {
					neighborLevel[location.ordinal()] = getSignalLevel(container, location);
					neighborValue[location.ordinal()] = getRedstoneLevel(container, location);
				}
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (facing == location.facing() && type == WireKind.NORMAL) {
				BlockPos pos = container.getPos().offset(facing);
				int i = 0;

				for (EnumFacing enumfacing : EnumFacing.values()) {
					IBlockState state = container.getWorld().getBlockState(pos.offset(enumfacing));
					Block block = state.getBlock();

					if (!(block instanceof BlockWire)) {
						int power = block.shouldCheckWeakPower(container.getWorld(), pos, enumfacing)
								? block.getStrongPower(container.getWorld(), pos, state, enumfacing)
								: block.getWeakPower(container.getWorld(), pos, state, enumfacing);

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
					neighborValue[facing.ordinal()] = (byte) i;
					neighborLevel[facing.ordinal()] = 255;
				}
			} else if (connectsExternal(facing)) {
				TileEntity tile = container.getNeighbourTile(facing);

				if (tile instanceof TileWireContainer) {
					neighborLevel[facing.ordinal()] = getSignalLevel((TileWireContainer) tile, location);
					neighborValue[facing.ordinal()] = getRedstoneLevel((TileWireContainer) tile, location);
				} else if (tile instanceof IRedstoneEmitter) {
					int value = ((IRedstoneEmitter) tile).getRedstoneSignal(location, facing.getOpposite());
					if (value > 0) {
						neighborValue[facing.ordinal()] = (byte) value;
						neighborLevel[facing.ordinal()] = 255;
					}
				} else {
					BlockPos pos = container.getPos().offset(facing);
					IBlockState state = container.getWorld().getBlockState(pos);
					Block block = state.getBlock();

					int power = block.shouldCheckWeakPower(container.getWorld(), pos, facing)
							? block.getStrongPower(container.getWorld(), pos, state, facing)
							: block.getWeakPower(container.getWorld(), pos, state, facing);

					if (power > 0) {
						neighborValue[facing.ordinal()] = (byte) power;
						neighborLevel[facing.ordinal()] = 255;
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = container.getPos().offset(facing).offset(location.facing());
				TileEntity tile = container.getWorld().getTileEntity(cornerPos);

				if (tile instanceof TileWireContainer) {
					neighborValue[facing.ordinal()] = getRedstoneLevel((TileWireContainer) tile, WireFace.get(facing.getOpposite()));
					neighborLevel[facing.ordinal()] = getSignalLevel((TileWireContainer) tile, WireFace.get(facing.getOpposite()));
				}
			}
		}

		for (int i = 0; i < 7; i++) {
			byte v = (byte) Math.min(neighborValue[i], 15);
			if (v > maxValue || (maxValue > 0 && v == maxValue && neighborLevel[i] > maxSignal)) {
				neighbor = i;
				maxSignal = neighborLevel[i];
				maxValue = v;
			}
		}

		if (maxSignal > signalLevel && maxSignal > 1) {
			signalLevel = maxSignal - 1;
			signalValue = maxValue;
		} else {
			signalLevel = 0;
			signalValue = 0;
		}

		if (DEBUG) {
			System.out.println("Levels: " + Arrays.toString(neighborLevel) + " " + Arrays.toString(neighborValue));
			System.out.println("Switch: " + oldSignal + ", " + oldValue + " -> " + signalLevel + ", " + signalValue);
		}

		if (signalLevel == 0) {
			for (WireFace nLoc : WireFace.VALUES) {
				if (connectsInternal(nLoc) && neighborLevel[nLoc.ordinal()] > 0) {
					container.updateWireLocation(nLoc);
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing();

					if (connectsExternal(facing)) {
						TileEntity tileEntity = container.getNeighbourTile(facing);
						if (!(tileEntity instanceof TileWireContainer) || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing);
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[facing.ordinal()] > 0) {
							propagateNotifyCorner(location.facing(), facing);
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				if (connectsInternal(nLoc) && neighborLevel[nLoc.ordinal()] < signalLevel - 1) {
					container.updateWireLocation(nLoc);
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing();

					if (connectsExternal(facing)) {
						if (neighborLevel[facing.ordinal()] < signalLevel - 1) {
							propagateNotify(facing);
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[facing.ordinal()] < signalLevel - 1) {
							propagateNotifyCorner(location.facing(), facing);
						}
					}
				}
			}
		}

		if (type == WireKind.NORMAL) {
			container.scheduleRenderUpdate();

			if (location != WireFace.CENTER) {
				propagateNotify(location.facing());
			}
		}
	}

	@Override
	public int getSignalLevel() {
		return signalLevel;
	}

	@Override
	public int getRedstoneLevel() {
		return signalValue;
	}
}
