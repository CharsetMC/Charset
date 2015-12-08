package pl.asie.charset.wires.logic;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.wires.TileWireContainer;
import pl.asie.charset.wires.WireType;
import pl.asie.charset.wires.internal.IRedstoneUpdatable;
import pl.asie.charset.wires.internal.WireLocation;

public abstract class Wire {
	protected static final boolean DEBUG = false;

	public final WireType type;
	public final WireLocation location;
	public final TileWireContainer container;
	protected byte internalConnections, externalConnections, cornerConnections;

	public Wire(WireType type, WireLocation location, TileWireContainer container) {
		this.type = type;
		this.location = location;
		this.container = container;
	}

	public abstract void propagate();
	public abstract int getSignalLevel();
	public abstract int getRedstoneLevel();

	public void readFromNBT(NBTTagCompound nbt) {
		internalConnections = nbt.getByte("iC");
		externalConnections = nbt.getByte("eC");
		cornerConnections = nbt.getByte("cC");
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setByte("iC", internalConnections);
		nbt.setByte("eC", externalConnections);
		if (location != WireLocation.FREESTANDING) {
			nbt.setByte("cC", cornerConnections);
		}
	}

	public void updateConnections() {
		Set<WireLocation> validSides = EnumSet.noneOf(WireLocation.class);

		for (WireLocation facing : WireLocation.VALUES) {
			if (facing == location) {
				continue;
			}

			if (facing != WireLocation.FREESTANDING && location != WireLocation.FREESTANDING && location.facing().getAxis() == facing.facing().getAxis()) {
				continue;
			}

			validSides.add(facing);
		}

		int oldConnectionCache = internalConnections << 12 | externalConnections << 6 | cornerConnections;

		internalConnections = externalConnections = cornerConnections = 0;

		for (WireLocation facing : validSides) {
			if (container.canConnectInternal(location, facing)) {
				internalConnections |= 1 << facing.ordinal();
			} else if (facing != WireLocation.FREESTANDING) {
				if (container.canConnectExternal(location, facing)) {
					externalConnections |= 1 << facing.ordinal();
				} else if (location != WireLocation.FREESTANDING && container.canConnectCorner(location, facing)) {
					cornerConnections |= 1 << facing.ordinal();
				}
			}
		}

		int newConnectionCache = internalConnections << 12 | externalConnections << 6 | cornerConnections;

		if (oldConnectionCache != newConnectionCache) {
			container.scheduleNeighborUpdate();
			container.schedulePropagationUpdate();
			container.scheduleRenderUpdate();
		}
	}

	protected void propagateNotifyCorner(EnumFacing side, EnumFacing direction) {
		BlockPos cornerPos = container.getPos().offset(side).offset(direction);
		TileEntity tile = container.getWorld().getTileEntity(cornerPos);
		if (tile instanceof TileWireContainer) {
			((TileWireContainer) tile).onWireUpdate(null);
		}
	}

	protected void propagateNotifyDelayed(EnumFacing facing) {
		container.getWorld().scheduleBlockUpdate(container.getPos().offset(facing), container.getBlockType(), 1, 0);
	}

	protected void propagateNotify(EnumFacing facing) {
		TileEntity nt = container.getNeighbourTile(facing);
		if (nt instanceof TileWireContainer) {
			((TileWireContainer) nt).updateWireLocation(location);
		} else if (nt instanceof IRedstoneUpdatable) {
			((IRedstoneUpdatable) nt).onRedstoneInputChanged();
		} else {
			container.getWorld().notifyBlockOfStateChange(container.getPos().offset(facing), container.getBlockType());
		}
	}

	public boolean connectsInternal(WireLocation side) {
		return (internalConnections & (1 << side.ordinal())) != 0;
	}

	public boolean connectsExternal(EnumFacing side) {
		return (externalConnections & (1 << side.ordinal())) != 0;
	}

	public boolean connectsAny(EnumFacing direction) {
		return ((internalConnections | externalConnections | cornerConnections) & (1 << direction.ordinal())) != 0;
	}

	public boolean connectsCorner(EnumFacing direction) {
		return (cornerConnections & (1 << direction.ordinal())) != 0;
	}

	public boolean connects(EnumFacing direction) {
		return ((internalConnections | externalConnections) & (1 << direction.ordinal())) != 0;
	}

	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		return -1;
	}

	public int getBundledSignalLevel(int i) {
		return 0;
	}
}
