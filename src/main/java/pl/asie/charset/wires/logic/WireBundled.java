package pl.asie.charset.wires.logic;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireType;
import pl.asie.charset.wires.internal.WireLocation;

public class WireBundled extends Wire {
	private int[] signalLevel = new int[16];

	public WireBundled(WireType type, WireLocation location, TileWireContainer container) {
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
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setIntArray("s", signalLevel);
	}

	@Override
	public void propagate() {
		if (DEBUG) {
			System.out.println("--- B! PROPAGATE " + container.getPos().toString() + " " + location.name() + " ---");
		}

		for (int i = 0; i < 16; i++) {
			int maxSignal = 0;
			int oldSignal = signalLevel[i];
			int[] neighborLevel = new int[7];

			if (internalConnections > 0) {
				for (WireLocation location : WireLocation.VALUES) {
					if (connectsInternal(location)) {
						neighborLevel[location.ordinal()] = container.getBundledSignalLevel(location, i);
					}
				}
			}

			for (EnumFacing facing : EnumFacing.VALUES) {
				if (connectsExternal(facing)) {
					TileEntity tile = container.getNeighbourTile(facing);

					if (tile instanceof TileWireContainer) {
						neighborLevel[facing.ordinal()] = ((TileWireContainer) tile).getBundledSignalLevel(location, i);
					}
				} else if (connectsCorner(facing)) {
					BlockPos cornerPos = container.getPos().offset(facing).offset(location.facing());
					TileEntity tile = container.getWorld().getTileEntity(cornerPos);

					if (tile instanceof TileWireContainer) {
						neighborLevel[facing.ordinal()] = ((TileWireContainer) tile).getBundledSignalLevel(WireLocation.get(facing.getOpposite()), i);
					}
				}
			}

			for (int j = 0; j < 7; j++) {
				maxSignal = Math.max(maxSignal, neighborLevel[j]);
			}

			if (DEBUG) {
				System.out.println("[" + i + "] Levels: " + Arrays.toString(neighborLevel));
			}

			if (maxSignal > signalLevel[i] && maxSignal > 1) {
				signalLevel[i] = maxSignal - 1;
			} else {
				signalLevel[i] = 0;
			}

			if (signalLevel[i] == oldSignal) {
				continue;
			}

			if (signalLevel[i] == 0) {
				for (WireLocation nLoc : WireLocation.VALUES) {
					if (connectsInternal(nLoc) && neighborLevel[nLoc.ordinal()] > 0) {
						container.updateWireLocation(nLoc);
					} else if (nLoc != WireLocation.FREESTANDING) {
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
				for (WireLocation nLoc : WireLocation.VALUES) {
					if (connectsInternal(nLoc) && neighborLevel[nLoc.ordinal()] < signalLevel[i] - 1) {
						container.updateWireLocation(nLoc);
					} else if (nLoc != WireLocation.FREESTANDING) {
						EnumFacing facing = nLoc.facing();

						if (connectsExternal(facing)) {
							if (neighborLevel[facing.ordinal()] < signalLevel[i] - 1) {
								propagateNotify(facing);
							}
						} else if (connectsCorner(facing)) {
							if (neighborLevel[facing.ordinal()] < signalLevel[i] - 1) {
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
	public int getSignalLevel() {
		return 0;
	}

	@Override
	public int getRedstoneLevel() {
		return 0;
	}
}
