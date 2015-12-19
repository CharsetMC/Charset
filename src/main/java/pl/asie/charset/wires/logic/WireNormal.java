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
import pl.asie.charset.wires.WireUtils;

public class WireNormal extends Wire {
	private int signalLevel;

	public WireNormal(WireKind type, WireFace location, TileWireContainer container) {
		super(type, location, container);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		if (type.type() == WireType.INSULATED) {
			return EnumDyeColor.byMetadata(type.color()).getMapColor().colorValue;
		} else {
			int signalValue = signalLevel >> 8;
			int v = (signalValue > 0 ? 0x96 : 0x78) + (signalValue * 7);
			return (v << 16) | (v << 8) | v;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		signalLevel = nbt.getShort("s");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, boolean isPacket) {
		super.writeToNBT(nbt, isPacket);
		if (!isPacket || type == WireKind.NORMAL) {
			nbt.setShort("s", (short) signalLevel);
		}
	}

	protected int getSignalLevel(TileWireContainer container, WireFace location) {
		return container.getSignalLevel(location);
	}

	@Override
	public void propagate(int color) {
		if (DEBUG) {
			System.out.println("--- PROPAGATE " + container.getPos().toString() + " " + location.name() + " ---");
		}

		int maxSignal = 0;
		int oldSignal = signalLevel;
		int[] neighborLevel = new int[7];

		if (type == WireKind.NORMAL) {
			if (location != WireFace.CENTER) {
				EnumFacing facing = location.facing();

				BlockPos pos = container.getPos().offset(facing);
				IBlockState state = container.getWorld().getBlockState(pos);

				int power = WireUtils.getRedstoneLevel(container.getWorld(), pos, state, facing);

				if (power > 0) {
					neighborLevel[facing.ordinal()] = Math.min(15, power) << 8 | 0xFF;
				}
			}
		}

		if (internalConnections > 0) {
			for (WireFace location : WireFace.VALUES) {
				if (connectsInternal(location)) {
					neighborLevel[location.ordinal()] = getSignalLevel(container, location);
				}
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			int facidx = facing.ordinal();

			if (facing == location.facing() && type == WireKind.NORMAL) {
				BlockPos pos = container.getPos().offset(facing);
				int i = 0;

				for (EnumFacing enumfacing : EnumFacing.values()) {
					IBlockState state = container.getWorld().getBlockState(pos.offset(enumfacing));
					Block block = state.getBlock();

					if (!(block instanceof BlockWire) && !(block instanceof BlockRedstoneWire)) {
						int power = WireUtils.getStrongLevel(container.getWorld(), pos, state, enumfacing);

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
				TileEntity tile = container.getNeighbourTile(facing);

				if (tile instanceof TileWireContainer) {
					neighborLevel[facidx] = getSignalLevel((TileWireContainer) tile, location);
				} else if (tile instanceof IRedstoneEmitter) {
					int value = ((IRedstoneEmitter) tile).getRedstoneSignal(location, facing.getOpposite());
					if (value > 0) {
						neighborLevel[facidx] = (Math.min(value, 15) << 8) | 0xFF;
					}
				} else {
					BlockPos pos = container.getPos().offset(facing);
					IBlockState state = container.getWorld().getBlockState(pos);

					int power = WireUtils.getRedstoneLevel(container.getWorld(), pos, state, facing);

					if (state.getBlock() instanceof BlockRedstoneWire) {
						power--;
					}

					if (power > 0) {
						neighborLevel[facidx] = (Math.min(power, 15) << 8) | 0xFF;
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = container.getPos().offset(facing).offset(location.facing());
				TileEntity tile = container.getWorld().getTileEntity(cornerPos);

				if (tile instanceof TileWireContainer) {
					neighborLevel[facidx] = getSignalLevel((TileWireContainer) tile, WireFace.get(facing.getOpposite()));
				}
			}
		}

		for (int i = 0; i < 7; i++) {
			if (neighborLevel[i] > maxSignal) {
				maxSignal = neighborLevel[i];
			}
		}

		if (maxSignal > signalLevel) {
			signalLevel = maxSignal - 1;
			if ((signalLevel & 0xFF) == 0 || (signalLevel & 0xFF) == 0xFF) {
				signalLevel = 0;
			}
		} else {
			signalLevel = 0;
		}

		if (DEBUG) {
            System.out.println("ConnectionCache: " + Integer.toBinaryString(internalConnections) + " " + Integer.toBinaryString(externalConnections) + " " + Integer.toBinaryString(cornerConnections));
			System.out.println("Levels: " + Arrays.toString(neighborLevel));
			System.out.println("Switch: " + oldSignal + " -> " + signalLevel);
		}

		if (signalLevel == 0) {
			for (WireFace nLoc : WireFace.VALUES) {
				if (connectsInternal(nLoc)) {
					if (neighborLevel[nLoc.ordinal()] > 0) {
						container.updateWireLocation(nLoc, type.color());
					}
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing();

					if (connectsExternal(facing)) {
						TileEntity tileEntity = container.getNeighbourTile(facing);
						if (!(tileEntity instanceof TileWireContainer) || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing, type.color());
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[nLoc.ordinal()] > 0) {
							propagateNotifyCorner(location.facing(), facing, type.color());
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
						container.updateWireLocation(nLoc, type.color());
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing();

						if (connectsExternal(facing)) {
							propagateNotify(facing, type.color());
						} else if (connectsCorner(facing)) {
							propagateNotifyCorner(location.facing(), facing, type.color());
						} else if (type == WireKind.NORMAL && facing.getOpposite() != location.facing()) {
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
			if ((oldSignal & 0xF00) != (signalLevel & 0xF00)) {
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
        System.out.println("LEVEL @ " + container.getPos() + " = " + signalLevel);
		return signalLevel;
	}

	@Override
	public int getRedstoneLevel() {
		return signalLevel >> 8;
	}
}
