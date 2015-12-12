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
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireKind;

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

	private void propagate(int i, byte[][] nValues) {
		int maxSignal = 0;
		int[] neighborLevel = new int[7];
		// [bundled] byte[] neighborValue = new byte[7];
		// [bundled] byte maxValue = 0;

		if (internalConnections > 0) {
			for (WireFace location : WireFace.VALUES) {
				if (connectsInternal(location)) {
					neighborLevel[location.ordinal()] = container.getBundledSignalLevel(location, i);
					// [bundled] neighborValue[location.ordinal()] = container.getBundledRedstoneLevel(location, i);
				}
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (connectsExternal(facing)) {
				if (nValues[facing.ordinal()] != null && nValues[facing.ordinal()][i] > 0) {
					neighborLevel[facing.ordinal()] = 255;
					// [bundled] neighborValue[facing.ordinal()] = nValues[facing.ordinal()][i];
				} else {
					TileEntity tile = container.getNeighbourTile(facing);

					if (tile instanceof TileWireContainer) {
						neighborLevel[facing.ordinal()] = ((TileWireContainer) tile).getBundledSignalLevel(location, i);
						// [bundled] neighborValue[facing.ordinal()] = ((TileWireContainer) tile).getBundledRedstoneLevel(location, i);
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = container.getPos().offset(facing).offset(location.facing());
				TileEntity tile = container.getWorld().getTileEntity(cornerPos);

				if (tile instanceof TileWireContainer) {
					neighborLevel[facing.ordinal()] = ((TileWireContainer) tile).getBundledSignalLevel(WireFace.get(facing.getOpposite()), i);
					// [bundled] neighborValue[facing.ordinal()] = ((TileWireContainer) tile).getBundledRedstoneLevel(WireFace.get(facing.getOpposite()), i);
				}
			}
		}

		for (int j = 0; j < 7; j++) {
			// [bundled] byte v = (byte) Math.min(neighborValue[j], 15);
			if (neighborLevel[j] > maxSignal) {
				maxSignal = neighborLevel[j];
				// [bundled] maxValue = v;
			}
		}

		if (DEBUG) {
			System.out.println("[" + i + "] Levels: " + Arrays.toString(neighborLevel));
		}

		int newSignal = 0;
		signalValue[i] = 0;

		if (maxSignal > signalLevel[i] && maxSignal > 1) {
			newSignal = maxSignal - 1;
			signalValue[i] = 15;
		}

		signalLevel[i] = newSignal;

		if (newSignal == 0) {
			for (WireFace nLoc : WireFace.VALUES) {
				if (connectsInternal(nLoc) && neighborLevel[nLoc.ordinal()] > 0) {
					container.updateWireLocation(nLoc, type.color());
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing();

					if (connectsExternal(facing)) {
						TileEntity tileEntity = container.getNeighbourTile(facing);
						if (!(tileEntity instanceof TileWireContainer) || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing, i);
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[facing.ordinal()] > 0) {
							propagateNotifyCorner(location.facing(), facing, i);
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				if (neighborLevel[nLoc.ordinal()] < newSignal - 1) {
					if (connectsInternal(nLoc)) {
						container.updateWireLocation(nLoc, type.color());
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing();

						if (connectsExternal(facing)) {
							propagateNotify(facing, i);
						} else if (connectsCorner(facing)) {
							propagateNotifyCorner(location.facing(), facing, i);
						}
					}
				}
			}
		}
	}

	@Override
	public void propagate(int color) {
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

		if (color < 0) {
			for (int i = 0; i < 16; i++) {
				propagate(i, nValues);
			}
		} else {
			propagate(color, nValues);
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
