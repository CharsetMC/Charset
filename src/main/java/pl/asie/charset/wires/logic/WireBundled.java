package pl.asie.charset.wires.logic;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireKind;
import pl.asie.charset.api.wires.WireFace;

public class WireBundled extends Wire {
	private int[] signalLevel = new int[16];
	private byte[] signalValue = new byte[16];

	public WireBundled(WireKind type, WireFace location, TileWireContainer container) {
		super(type, location, container);
	}

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
		signalValue = nbt.getByteArray("v");
		if (signalValue == null || signalValue.length != 16) {
			signalValue = new byte[16];
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, boolean isPacket) {
		super.writeToNBT(nbt, isPacket);
		if (!isPacket) {
			nbt.setIntArray("s", signalLevel);
			nbt.setByteArray("v", signalValue);
		}
	}

	@Override
	public void propagate() {
		if (DEBUG) {
			System.out.println("--- B! PROPAGATE " + container.getPos().toString() + " " + location.name() + " ---");
		}

		byte[][] nValues = new byte[6][];

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (connectsExternal(facing)) {
				TileEntity tile = container.getNeighbourTile(facing);
				if (tile instanceof IBundledEmitter && !(tile instanceof IWire)) {
					nValues[facing.ordinal()] = ((IBundledEmitter) tile).getBundledSignal(location, facing.getOpposite());
				}
			}
		}

		for (int i = 0; i < 16; i++) {
			int maxSignal = 0;
			int oldSignal = signalLevel[i];
			byte oldValue = signalValue[i];
			int[] neighborLevel = new int[7];
			byte[] neighborValue = new byte[7];
			byte maxValue = 0;

			if (internalConnections > 0) {
				for (WireFace location : WireFace.VALUES) {
					if (connectsInternal(location)) {
						neighborLevel[location.ordinal()] = container.getBundledSignalLevel(location, i);
						neighborValue[location.ordinal()] = container.getBundledRedstoneLevel(location, i);
					}
				}
			}

			for (EnumFacing facing : EnumFacing.VALUES) {
				if (connectsExternal(facing)) {
					if (nValues[facing.ordinal()] != null && nValues[facing.ordinal()][i] > 0) {
						neighborLevel[facing.ordinal()] = 255;
						neighborValue[facing.ordinal()] = nValues[facing.ordinal()][i];
					} else {
						TileEntity tile = container.getNeighbourTile(facing);

						if (tile instanceof TileWireContainer) {
							neighborLevel[facing.ordinal()] = ((TileWireContainer) tile).getBundledSignalLevel(location, i);
							neighborValue[facing.ordinal()] = ((TileWireContainer) tile).getBundledRedstoneLevel(location, i);
						}
					}
				} else if (connectsCorner(facing)) {
					BlockPos cornerPos = container.getPos().offset(facing).offset(location.facing());
					TileEntity tile = container.getWorld().getTileEntity(cornerPos);

					if (tile instanceof TileWireContainer) {
						neighborLevel[facing.ordinal()] = ((TileWireContainer) tile).getBundledSignalLevel(WireFace.get(facing.getOpposite()), i);
						neighborValue[facing.ordinal()] = ((TileWireContainer) tile).getBundledRedstoneLevel(WireFace.get(facing.getOpposite()), i);
					}
				}
			}

			for (int j = 0; j < 7; j++) {
				byte v = (byte) Math.min(neighborValue[j], 15);
				if (v > maxValue || (maxValue > 0 && v == maxValue && neighborLevel[j] > maxSignal)) {
					maxSignal = neighborLevel[j];
					maxValue = v;
				}
			}

			if (DEBUG) {
				System.out.println("[" + i + "] Levels: " + Arrays.toString(neighborLevel));
			}

			int newSignal = 0;
			signalValue[i] = 0;

			if (maxSignal > signalLevel[i] && maxSignal > 1) {
				newSignal = maxSignal - 1;
				signalValue[i] = maxValue;
			}

			signalLevel[i] = newSignal;

			if (newSignal == oldSignal && signalValue[i] == oldValue) {
				continue;
			}

			if (newSignal == 0) {
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
					if (connectsInternal(nLoc) && neighborLevel[nLoc.ordinal()] < newSignal - 1) {
						container.updateWireLocation(nLoc);
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing();

						if (connectsExternal(facing)) {
							if (neighborLevel[facing.ordinal()] < newSignal - 1) {
								propagateNotify(facing);
							}
						} else if (connectsCorner(facing)) {
							if (neighborLevel[facing.ordinal()] < newSignal - 1) {
								propagateNotifyCorner(location.facing(), facing);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public int getBundledSignalLevel(int i) {
		return signalLevel[i & 15];
	}

	@Override
	public byte getBundledRedstoneLevel(int i) {
		return signalValue[i & 15];
	}

	@Override
	public int getSignalLevel() {
		return 0;
	}

	@Override
	public int getRedstoneLevel() {
		return 0;
	}

	public byte[] getBundledSignal() {
		return signalValue;
	}
}
