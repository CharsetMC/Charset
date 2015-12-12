package pl.asie.charset.wires.logic;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneUpdatable;
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
	public void writeToNBT(NBTTagCompound nbt, boolean isPacket) {
		super.writeToNBT(nbt, isPacket);
		if (!isPacket) {
			nbt.setShort("s", (short) signalLevel);
		}
		if (!isPacket || type == WireKind.NORMAL) {
			nbt.setByte("v", signalValue);
		}
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
		// [bundled] byte maxValue = 0;
		int oldSignal = signalLevel;
		byte oldValue = signalValue;
		int[] neighborLevel = new int[7];
		byte[] neighborValue = new byte[7];

		if (type == WireKind.NORMAL) {
			if (location != WireFace.CENTER) {
				EnumFacing facing = location.facing();

				BlockPos pos = container.getPos().offset(facing);
				IBlockState state = container.getWorld().getBlockState(pos);
				Block block = state.getBlock();

				int power = block.shouldCheckWeakPower(container.getWorld(), pos, facing)
						? block.getStrongPower(container.getWorld(), pos, state, facing)
						: block.getWeakPower(container.getWorld(), pos, state, facing);

				if (power > 0) {
					if (!(block instanceof BlockRedstoneWire && oldSignal > 0)) {
						// [bundled] neighborValue[facing.ordinal()] = (byte) power;
						neighborLevel[facing.ordinal()] = 255;
					}
				}
			}
		}

		if (internalConnections > 0) {
			for (WireFace location : WireFace.VALUES) {
				if (connectsInternal(location)) {
					neighborLevel[location.ordinal()] = getSignalLevel(container, location);
					// [bundled] neighborValue[location.ordinal()] = getRedstoneLevel(container, location);
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
					// [bundled] neighborValue[facing.ordinal()] = (byte) i;
					neighborLevel[facing.ordinal()] = 255;
				}
			} else if (connectsExternal(facing)) {
				TileEntity tile = container.getNeighbourTile(facing);

				if (tile instanceof TileWireContainer) {
					neighborLevel[facing.ordinal()] = getSignalLevel((TileWireContainer) tile, location);
					// [bundled] neighborValue[facing.ordinal()] = getRedstoneLevel((TileWireContainer) tile, location);
				} else if (tile instanceof IRedstoneEmitter) {
					int value = ((IRedstoneEmitter) tile).getRedstoneSignal(location, facing.getOpposite());
					if (value > 0) {
						// [bundled] neighborValue[facing.ordinal()] = (byte) value;
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
						if (block instanceof BlockRedstoneWire && oldSignal > 0) {
							continue;
						}
						// [bundled] neighborValue[facing.ordinal()] = (byte) power;
						neighborLevel[facing.ordinal()] = 255;
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = container.getPos().offset(facing).offset(location.facing());
				TileEntity tile = container.getWorld().getTileEntity(cornerPos);

				if (tile instanceof TileWireContainer) {
					// [bundled] neighborValue[facing.ordinal()] = getRedstoneLevel((TileWireContainer) tile, WireFace.get(facing.getOpposite()));
					neighborLevel[facing.ordinal()] = getSignalLevel((TileWireContainer) tile, WireFace.get(facing.getOpposite()));
				}
			}
		}

		for (int i = 0; i < 7; i++) {
			if (neighborLevel[i] > 1) {
				// [bundled] byte v = (byte) Math.min(neighborValue[i], 15);
				if (neighborLevel[i] > maxSignal) {
					maxSignal = neighborLevel[i];
					// [bundled] maxValue = v;
				}
			}
		}

		if (maxSignal > signalLevel && maxSignal > 1) {
			signalLevel = maxSignal - 1;
			signalValue = 15;
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
				if (connectsInternal(nLoc)) {
					if (neighborLevel[nLoc.ordinal()] > 0) {
						container.updateWireLocation(nLoc);
					}
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing();

					if (connectsExternal(facing)) {
						TileEntity tileEntity = container.getNeighbourTile(facing);
						if (!(tileEntity instanceof TileWireContainer) || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing);
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[nLoc.ordinal()] > 0) {
							propagateNotifyCorner(location.facing(), facing);
						}
					} else if (type == WireKind.NORMAL && facing.getOpposite() != location.facing()) {
						TileEntity nt = container.getNeighbourTile(facing);
						if (!(nt instanceof IRedstoneUpdatable)) {
							container.getWorld().notifyBlockOfStateChange(container.getPos().offset(facing), container.getBlockType());
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				if (neighborLevel[nLoc.ordinal()] < signalLevel - 1) {
					if (connectsInternal(nLoc)) {
						container.updateWireLocation(nLoc);
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing();

						if (connectsExternal(facing)) {
							propagateNotify(facing);
						} else if (connectsCorner(facing)) {
							propagateNotifyCorner(location.facing(), facing);
						} else if (type == WireKind.NORMAL) {
							TileEntity nt = container.getNeighbourTile(facing);
							if (!(nt instanceof IRedstoneUpdatable)) {
								container.getWorld().notifyBlockOfStateChange(container.getPos().offset(facing), container.getBlockType());
							}
						}
					}
				}
			}
		}

		if (type == WireKind.NORMAL) {
			if (oldValue != signalValue) {
				container.scheduleRenderUpdate();

				if (location != WireFace.CENTER) {
					BlockPos uPos = container.getPos().offset(location.facing());
					container.getWorld().notifyNeighborsOfStateExcept(uPos, container.getBlockType(), location.facing().getOpposite());
				}
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
