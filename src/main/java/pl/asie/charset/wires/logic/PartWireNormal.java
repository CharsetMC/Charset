package pl.asie.charset.wires.logic;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import pl.asie.charset.api.wires.IRedstoneUpdatable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.wires.WireKind;
import pl.asie.charset.wires.WireUtils;

public class PartWireNormal extends PartWireBase {
	private int signalLevel;

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
    public void readUpdatePacket(PacketBuffer data) {
        super.readUpdatePacket(data);
        if (type == WireKind.NORMAL) {
            signalLevel = data.readByte() << 8;
        }
    }

    @Override
    public void writeUpdatePacket(PacketBuffer data) {
        super.writeUpdatePacket(data);
        if (type == WireKind.NORMAL) {
            data.writeByte(signalLevel >> 8);
        }
    }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		signalLevel = nbt.getShort("s");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setShort("s", (short) signalLevel);
	}

    @Override
    protected void onSignalChanged(int color) {
        propagate(color);
    }

    protected int getRedstoneLevel(IMultipartContainer container, WireFace location) {
		return WireUtils.getRedstoneWireLevel(container, location);
	}

	@Override
	public void propagate(int color) {
		if (DEBUG) {
			System.out.println("--- PROPAGATE " + getPos().toString() + " " + location.name() + " ---");
		}

		int maxSignal = 0;
		int oldSignal = signalLevel;
		int[] neighborLevel = new int[7];

		if (type == WireKind.NORMAL) {
			if (location != WireFace.CENTER) {
				EnumFacing facing = location.facing;

				BlockPos pos = getPos().offset(facing);
				IBlockState state = getWorld().getBlockState(pos);

				int power = WireUtils.getRedstoneLevel(getWorld(), pos, state, facing, location, false);

				if (power > 0) {
					neighborLevel[facing.ordinal()] = Math.min(15, power) << 8 | 0xFF;
				}
			}
		}

		if (internalConnections > 0) {
			for (WireFace location : WireFace.VALUES) {
				if (connectsInternal(location)) {
					neighborLevel[location.ordinal()] = getRedstoneLevel(getContainer(), location);
				}
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			int facidx = facing.ordinal();

			if (facing == location.facing && type == WireKind.NORMAL) {
				BlockPos pos = getPos().offset(facing);
				int i = 0;

				for (EnumFacing enumfacing : EnumFacing.values()) {
					IBlockState state = getWorld().getBlockState(pos.offset(enumfacing));
					Block block = state.getBlock();

					if (!(block instanceof BlockRedstoneWire)) {
						int power = WireUtils.getRedstoneLevel(getWorld(), pos.offset(enumfacing), state, enumfacing, location, true);

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
                BlockPos pos = getPos().offset(facing);
                IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), pos);

                if (WireUtils.getWire(container, location) != null) {
                    neighborLevel[facidx] = getRedstoneLevel(container, location);
                } else {
                    IBlockState state = getWorld().getBlockState(pos);

                    int power = WireUtils.getRedstoneLevel(getWorld(), pos, state, facing, location, true);

                    if (state.getBlock() instanceof BlockRedstoneWire) {
                        power--;
                    }

                    if (power > 0) {
                        neighborLevel[facidx] = (Math.min(power, 15) << 8) | 0xFF;
                    }
                }
			} else if (connectsCorner(facing)) {
                IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing).offset(location.facing));
                if (container != null) {
                    neighborLevel[facidx] = getRedstoneLevel(container, WireFace.get(facing.getOpposite()));
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
						WireUtils.getWire(getContainer(), nLoc).onSignalChanged(type.color());
					}
				} else if (nLoc != WireFace.CENTER) {
					EnumFacing facing = nLoc.facing;

					if (connectsExternal(facing)) {
                        IMultipartContainer container = MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing));
						if (container == null || WireUtils.getWire(container, location) == null || neighborLevel[facing.ordinal()] > 0) {
							propagateNotify(facing, type.color());
						}
					} else if (connectsCorner(facing)) {
						if (neighborLevel[nLoc.ordinal()] > 0) {
							propagateNotifyCorner(location.facing, facing, type.color());
						}
					} else if (type == WireKind.NORMAL && facing.getOpposite() != location.facing) {
						TileEntity nt = getWorld().getTileEntity(getPos().offset(facing));
						if (!(nt instanceof IRedstoneUpdatable)) {
							getWorld().notifyBlockOfStateChange(getPos().offset(facing), MCMultiPartMod.multipart);
						}
					}
				}
			}
		} else {
			for (WireFace nLoc : WireFace.VALUES) {
				if (neighborLevel[nLoc.ordinal()] < signalLevel - 1) {
					if (connectsInternal(nLoc)) {
                        WireUtils.getWire(getContainer(), nLoc).onSignalChanged(type.color());
					} else if (nLoc != WireFace.CENTER) {
						EnumFacing facing = nLoc.facing;

						if (connectsExternal(facing)) {
							propagateNotify(facing, type.color());
						} else if (connectsCorner(facing)) {
							propagateNotifyCorner(location.facing, facing, type.color());
						} else if (type == WireKind.NORMAL && facing.getOpposite() != location.facing) {
                            TileEntity nt = getWorld().getTileEntity(getPos().offset(facing));
                            if (!(nt instanceof IRedstoneUpdatable)) {
                                getWorld().notifyBlockOfStateChange(getPos().offset(facing), MCMultiPartMod.multipart);
                            }
						}
					}
				}
			}
		}

		if (type == WireKind.NORMAL) {
			if ((oldSignal & 0xF00) != (signalLevel & 0xF00)) {
				scheduleRenderUpdate();

				if (location != WireFace.CENTER) {
					BlockPos uPos = getPos().offset(location.facing);
					getWorld().notifyNeighborsOfStateExcept(uPos, MCMultiPartMod.multipart, location.facing.getOpposite());
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
		return signalLevel >> 8;
	}
}
