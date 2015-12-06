package pl.asie.charset.wires;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.property.IUnlistedProperty;

public class TileWire extends TileEntity implements ITickable {
	public static final Property PROPERTY = new Property();

	public enum WireSide {
		DOWN,
		UP,
		NORTH,
		SOUTH,
		WEST,
		EAST,
		FREESTANDING;

		public static final WireSide[] VALUES = values();

		public EnumFacing facing() {
			return ordinal() >= 6 ? null : EnumFacing.getFront(ordinal());
		}

		public int meta() {
			return this == FREESTANDING ? 1 : 0;
		}

		public static WireSide get(EnumFacing facing) {
			return facing != null ? VALUES[facing.ordinal()] : FREESTANDING;
		}
	}

	private static class Property implements IUnlistedProperty<TileWire> {
		private Property() {

		}

		@Override
		public String getName() {
			return "wireTile";
		}

		@Override
		public boolean isValid(TileWire value) {
			return true;
		}

		@Override
		public Class<TileWire> getType() {
			return TileWire.class;
		}

		@Override
		public String valueToString(TileWire value) {
			return "!?";
		}
	}

	public TileEntity getNeighbourTile(EnumFacing side) {
		return side != null ? worldObj.getTileEntity(pos.offset(side)) : null;
	}

	private boolean scheduledRenderUpdate, scheduledConnectionUpdate, scheduledNeighborUpdate, scheduledPropagationUpdate;
	private int wireSet = 0, signalCache = 0, signalLevel = 0;
	private long connectionCache = 0;

	@Override
	public void validate() {
		super.validate();
		scheduleConnectionUpdate();
	}

	@Override
	public void update() {
		if (scheduledConnectionUpdate) {
			updateConnections();
			scheduledConnectionUpdate = false;
		}

		if (scheduledNeighborUpdate) {
			worldObj.notifyNeighborsRespectDebug(pos, getBlockType());
			scheduledNeighborUpdate = false;
		}

		if (scheduledPropagationUpdate) {
			propagate();
			scheduledPropagationUpdate = false;
		}

		if (scheduledRenderUpdate) {
			if (!worldObj.isRemote) {
				getWorld().markBlockForUpdate(pos);
			} else {
				getWorld().markBlockRangeForRenderUpdate(pos, pos);
			}
			scheduledRenderUpdate = false;
		}
	}

	public boolean canProvideStrongPower(EnumFacing direction) {
		return hasWire(WireSide.get(direction));
	}

	public boolean canProvideWeakPower(EnumFacing direction) {
		if (!providesSignal(direction)) {
			return false;
		}

		if (hasWire(WireSide.FREESTANDING)) {
			return true;
		}

		if (hasWire(WireSide.get(direction.getOpposite()))) {
			return false;
		}

		return true;
	}

	public int getRedstoneLevel() {
		return signalLevel > 0 ? 15 : 0;
	}

	public int getSignalLevel() {
		return signalLevel;
	}

	protected void propagate() {
		int maxSignal = 0;
		int oldSignal = signalLevel;
		int[] sl = new int[6];

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (providesSignal(facing)) {
				sl[facing.ordinal()] = WireUtils.getSignalLevel(worldObj, pos.offset(facing), facing);
				if (sl[facing.ordinal()] > maxSignal) {
					maxSignal = sl[facing.ordinal()];
				}
			}
		}

		if (maxSignal > signalLevel && maxSignal > 1) {
			signalLevel = maxSignal - 1;
		} else {
			signalLevel = 0;
		}

		if (signalLevel == oldSignal) {
			return;
		}

		scheduleRenderUpdate();

		if (signalLevel == 0) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (providesSignal(facing)) {
					TileEntity tileEntity = getNeighbourTile(facing);
					if (!(tileEntity instanceof TileWire) || ((TileWire) tileEntity).signalLevel > 0) {
						propagateNotify(facing);
					}
				}
			}
		} else {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (providesSignal(facing) && sl[facing.ordinal()] < signalLevel - 1) {
					propagateNotify(facing);
				}
			}
		}
	}

	private void propagateNotify(EnumFacing facing) {
		TileEntity nt = getNeighbourTile(facing);
		if (nt instanceof TileWire) {
			((TileWire) nt).propagate();
		} else {
			worldObj.notifyBlockOfStateChange(pos.offset(facing), getBlockType());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		wireSet = tag.getByte("wireSet");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("wireSet", (byte) wireSet);
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setShort("s", (short) signalLevel);
		tag.setByte("w", (byte) wireSet);
		tag.setLong("c", connectionCache);
		return new S35PacketUpdateTileEntity(getPos(), getBlockMetadata(), tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		wireSet = pkt.getNbtCompound().getByte("w");
		connectionCache = pkt.getNbtCompound().getLong("c");
		signalLevel = pkt.getNbtCompound().getShort("s");
		scheduleRenderUpdate();
	}

	public boolean setWire(WireSide side, boolean b) {
		if (!(b ^ hasWire(side))) {
			return false;
		}

		if (side != WireSide.FREESTANDING && !WireUtils.canPlaceWire(worldObj, pos.offset(side.facing()), side.facing().getOpposite())) {
			return false;
		}

		if (b) {
			this.wireSet |= (1 << side.ordinal());
		} else {
			this.wireSet &= ~(1 << side.ordinal());
		}

		scheduleConnectionUpdate();
		scheduleRenderUpdate();

		return true;
	}

	public void scheduleNeighborUpdate() {
		scheduledNeighborUpdate = true;
	}

	public void onNeighborBlockChange() {
		scheduleConnectionUpdate();
		schedulePropagationUpdate();
	}

	public void schedulePropagationUpdate() {
		scheduledPropagationUpdate = true;
	}

	public void scheduleRenderUpdate() {
		scheduledRenderUpdate = true;
	}

	public void scheduleConnectionUpdate() {
		if (!getWorld().isRemote) {
			scheduledConnectionUpdate = true;
		}
	}

	protected boolean hasWires() {
		return wireSet != 0;
	}

	public boolean hasWire(WireSide side) {
		return (wireSet & (1 << side.ordinal())) != 0;
	}

	private int getConnectionIndex(WireSide side, EnumFacing direction) {
		return side.ordinal() * 6 + direction.ordinal();
	}

	private boolean canConnect(WireSide side, EnumFacing direction) {
		return hasWire(side);
	}

	private boolean connectsInternal(WireSide side, EnumFacing direction) {
		EnumFacing sideF = side.facing();

		if (sideF != null && sideF.getAxis() == direction.getAxis()) {
			return false;
		}

		if (canConnect(WireSide.get(direction), sideF)) {
			return true;
		}

		BlockPos connectingPos = pos.offset(direction);
		IBlockState connectingState = worldObj.getBlockState(connectingPos);
		Block connectingBlock = connectingState.getBlock();

		if (connectingBlock instanceof BlockWire) {
			TileEntity connectingTile = getNeighbourTile(direction);
			if (connectingTile instanceof TileWire && ((TileWire) connectingTile).canConnect(side, direction.getOpposite())) {
				return true;
			}
		} else {
			if (connectingBlock instanceof BlockRedstoneDiode && side != WireSide.DOWN) {
				return false;
			}

			if (side == WireSide.FREESTANDING && !connectingBlock.isSideSolid(worldObj, connectingPos, direction.getOpposite())) {
				return false;
			}

			if (connectingBlock.canConnectRedstone(worldObj, connectingPos, direction.getOpposite())) {
				return true;
			}
		}

		return false;
	}

	public boolean providesSignal(EnumFacing side) {
		return (signalCache & (1 << side.ordinal())) != 0;
 	}

	private void updateConnections() {
		long oldConnectionCache = connectionCache;
		connectionCache = 0;
		signalCache = 0;

		for (WireSide side : WireSide.VALUES) {
			if (hasWire(side)) {
				for (EnumFacing facing : EnumFacing.VALUES) {
					if (connectsInternal(side, facing)) {
						connectionCache |= (long) 1 << getConnectionIndex(side, facing);
						signalCache |= 1 << facing.ordinal();
					}
				}
			}
		}

		if (oldConnectionCache != connectionCache) {
			scheduleNeighborUpdate();
			schedulePropagationUpdate();
			scheduleRenderUpdate();
		}
	}

	public boolean connects(WireSide side, EnumFacing direction) {
		return (connectionCache & ((long) 1 << getConnectionIndex(side, direction))) != 0;
	}
}
