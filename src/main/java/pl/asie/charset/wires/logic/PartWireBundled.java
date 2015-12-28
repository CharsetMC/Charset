package pl.asie.charset.wires.logic;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledWire;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.utils.MultipartUtils;
import pl.asie.charset.wires.WireUtils;

public class PartWireBundled extends PartWireBase implements IBundledWire {
	private int[] signalLevel = new int[16];
	private byte[] signalValue = new byte[16];

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
		for (int i = 0; i < 16; i++) {
			signalValue[i] = (byte) (signalLevel[i] >> 8);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
        nbt.setIntArray("s", signalLevel);
	}

	private void propagate(int color, byte[][] nValues) {
		int maxSignal = 0;
		int[] neighborLevel = new int[7];

		if (internalConnections > 0) {
			for (WireFace location : WireFace.VALUES) {
				if (connectsInternal(location)) {
					neighborLevel[location.ordinal()] = WireUtils.getBundledWireLevel(getContainer(), location, color);
				}
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (connectsExternal(facing)) {
				if (nValues[facing.ordinal()] != null) {
					neighborLevel[facing.ordinal()] = (nValues[facing.ordinal()][color] << 8) | 0xFF;
				} else {
                    IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing));
					if (container != null) {
						neighborLevel[facing.ordinal()] = WireUtils.getBundledWireLevel(container, location, color);
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = getPos().offset(facing).offset(location.facing);
                IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), cornerPos);
                if (container != null) {
                    neighborLevel[facing.ordinal()] = WireUtils.getBundledWireLevel(container, WireFace.get(facing.getOpposite()), color);
                }
			}
		}

		for (int j = 0; j < 7; j++) {
			if (neighborLevel[j] > maxSignal) {
				maxSignal = neighborLevel[j];
			}
		}

		if (DEBUG) {
			System.out.println("[" + color + "] Levels: " + Arrays.toString(neighborLevel));
		}

		int newSignal = 0;

		if (maxSignal > signalLevel[color] && maxSignal > 1) {
			newSignal = maxSignal - 1;
			if ((newSignal & 0xFF) == 0 || (newSignal & 0xFF) == 0xFF) {
				newSignal = 0;
			}
		}

		signalLevel[color] = newSignal;
		signalValue[color] = (byte) (newSignal >> 8);

		if (newSignal == 0) {
			for (WireFace nLoc : WireFace.VALUES) {
				if (connectsInternal(nLoc) && neighborLevel[nLoc.ordinal()] > 0) {
                    WireUtils.getWire(getContainer(), nLoc).onSignalChanged(color);
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing;

					if (connectsExternal(facing)) {
                        IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing));
                        if (container == null || WireUtils.getWire(container, location) == null || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing, color);
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[facing.ordinal()] > 0) {
							propagateNotifyCorner(location.facing, facing, color);
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				if (neighborLevel[nLoc.ordinal()] < newSignal - 1) {
					if (connectsInternal(nLoc)) {
                        WireUtils.getWire(getContainer(), nLoc).onSignalChanged(color);
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing;

						if (connectsExternal(facing)) {
							propagateNotify(facing, color);
						} else if (connectsCorner(facing)) {
							propagateNotifyCorner(location.facing, facing, color);
						}
					}
				}
			}
		}
	}

	@Override
	public void propagate(int color) {
		if (DEBUG) {
			System.out.println("--- B! PROPAGATE " + getPos().toString() + " " + location.name() + " --- " + color);
            System.out.println("ConnectionCache: " + Integer.toBinaryString(internalConnections) + " " + Integer.toBinaryString(externalConnections) + " " + Integer.toBinaryString(cornerConnections));
        }

		byte[][] nValues = new byte[6][];

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (connectsExternal(facing)) {
                IBundledEmitter emitter = MultipartUtils.getInterface(IBundledEmitter.class, getWorld(), getPos().offset(facing), location.facing, facing.getOpposite());

				if (emitter != null && !(emitter instanceof IWire)) {
					nValues[facing.ordinal()] = emitter.getBundledSignal(location, facing.getOpposite());
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
    protected void onSignalChanged(int color) {
        propagate(color);
    }

	@Override
	public int getBundledSignalLevel(int i) {
		return signalLevel[i & 15];
	}

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

    @Override
    public byte[] getBundledSignal(WireFace face, EnumFacing toDirection) {
        return face == location && connects(toDirection) ? signalValue : null;
    }

    @Override
    public void onBundledInputChanged(EnumFacing face) {
        if (connects(face)) {
            schedulePropagationUpdate();
        }
    }
}
