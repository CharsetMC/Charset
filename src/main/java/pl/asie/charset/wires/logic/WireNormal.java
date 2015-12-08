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

import pl.asie.charset.wires.BlockWire;
import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireType;
import pl.asie.charset.wires.internal.IRedstoneEmitter;
import pl.asie.charset.wires.internal.WireLocation;

public class WireNormal extends Wire {
	private int signalLevel;

	public WireNormal(WireType type, WireLocation location, TileWireContainer container) {
		super(type, location, container);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		return type.type() == WireType.Type.INSULATED ? EnumDyeColor.byMetadata(type.color()).getMapColor().colorValue : -1;
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

	protected int getSignalLevel(TileWireContainer container, WireLocation location) {
		return container.getSignalLevel(location);
	}

	@Override
	public void propagate() {
		if (DEBUG) {
			System.out.println("--- PROPAGATE " + container.getPos().toString() + " " + location.name() + " ---");
		}

		int maxSignal = 0;
		int oldSignal = signalLevel;
		int[] neighborLevel = new int[7];

		if (internalConnections > 0) {
			for (WireLocation location : WireLocation.VALUES) {
				if (connectsInternal(location)) {
					neighborLevel[location.ordinal()] = getSignalLevel(container, location);
				}
			}
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (facing == location.facing() && type == WireType.NORMAL) {
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
					neighborLevel[facing.ordinal()] = 255;
				}
			} else if (connectsExternal(facing)) {
				TileEntity tile = container.getNeighbourTile(facing);

				if (tile instanceof TileWireContainer) {
					neighborLevel[facing.ordinal()] = getSignalLevel((TileWireContainer) tile, location);
				} else if (tile instanceof IRedstoneEmitter) {
					neighborLevel[facing.ordinal()] = ((IRedstoneEmitter) tile).getSignalStrength(facing.getOpposite());
				} else {
					BlockPos pos = container.getPos().offset(facing);
					IBlockState state = container.getWorld().getBlockState(pos);
					Block block = state.getBlock();

					int power = block.shouldCheckWeakPower(container.getWorld(), pos, facing)
							? block.getStrongPower(container.getWorld(), pos, state, facing)
							: block.getWeakPower(container.getWorld(), pos, state, facing);

					if (power > 0) {
						neighborLevel[facing.ordinal()] = 255;
					}
				}
			} else if (connectsCorner(facing)) {
				BlockPos cornerPos = container.getPos().offset(facing).offset(location.facing());
				TileEntity tile = container.getWorld().getTileEntity(cornerPos);

				if (tile instanceof TileWireContainer) {
					neighborLevel[facing.ordinal()] = getSignalLevel((TileWireContainer) tile, WireLocation.get(facing.getOpposite()));
				}
			}
		}

		for (int i = 0; i < 7; i++) {
			maxSignal = Math.max(maxSignal, neighborLevel[i]);
		}

		if (maxSignal > signalLevel && maxSignal > 1) {
			signalLevel = maxSignal - 1;
		} else {
			signalLevel = 0;
		}

		if (DEBUG) {
			System.out.println("Levels: " + Arrays.toString(neighborLevel));
			System.out.println("Switch: " + oldSignal + " -> " + signalLevel);
		}

		if (signalLevel == 0) {
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
				if (connectsInternal(nLoc) && neighborLevel[nLoc.ordinal()] < signalLevel - 1) {
					container.updateWireLocation(nLoc);
				} else if (nLoc != WireLocation.FREESTANDING) {
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

		if (type == WireType.NORMAL) {
			container.scheduleRenderUpdate();

			if (location != WireLocation.FREESTANDING) {
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
		return signalLevel > 0 ? 15 : 0;
	}
}
