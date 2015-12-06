package pl.asie.charset.wires;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.property.IUnlistedProperty;

import pl.asie.charset.wires.internal.IConnectable;
import pl.asie.charset.wires.internal.IRedstoneUpdatable;
import pl.asie.charset.wires.internal.IRedstoneWire;
import pl.asie.charset.wires.internal.WireLocation;

public class TileWire extends TileEntity implements ITickable, IRedstoneWire {
	public static final Property PROPERTY = new Property();

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
	private long cornerConnectionCache = 0;
	private long anyConnectionCache = 0;

	public int getRenderColor() {
		return -1;
	}

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
			for (EnumFacing facing : EnumFacing.VALUES) {
				worldObj.notifyNeighborsOfStateExcept(pos.offset(facing), getBlockType(), facing.getOpposite());
			}
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
		return hasWire(WireLocation.get(direction));
	}

	public boolean canProvideWeakPower(EnumFacing direction) {
		if (!providesSignal(direction)) {
			return false;
		}

		if (hasWire(WireLocation.FREESTANDING)) {
			return true;
		}

		if (hasWire(WireLocation.get(direction.getOpposite()))) {
			return false;
		}

		return true;
	}

	public int getItemMetadata(WireLocation loc) {
		return loc == WireLocation.FREESTANDING ? 1 : 0;
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
		int[] cl = new int[36];

		if (signalCache > 0) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (providesSignal(facing)) {
					sl[facing.ordinal()] = WireUtils.getSignalLevel(worldObj, pos.offset(facing), facing);
					if (sl[facing.ordinal()] > maxSignal) {
						maxSignal = sl[facing.ordinal()];
					}
				}
			}
		}

		if (cornerConnectionCache > 0) {
			for (EnumFacing side : EnumFacing.VALUES) {
				for (EnumFacing direction : EnumFacing.VALUES) {
					if (direction.getAxis() == side.getAxis() || !connectsCorner(WireLocation.get(side), direction)) {
						continue;
					}

					BlockPos cornerPos = pos.offset(side).offset(direction);
					TileEntity tile = worldObj.getTileEntity(cornerPos);
					if (tile instanceof TileWire) {
						int i = side.ordinal() * 6 + direction.ordinal();
						cl[i] = ((TileWire) tile).getSignalLevel();
						if (cl[i] > maxSignal) {
							maxSignal = cl[i];
						}
					}
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

			for (EnumFacing side : EnumFacing.VALUES) {
				for (EnumFacing direction : EnumFacing.VALUES) {
					if (direction.getAxis() == side.getAxis()) {
						continue;
					}

					if (cl[direction.ordinal() + side.ordinal() * 6] > 0) {
						propagateNotifyCorner(side, direction);
					}
				}
			}
		} else {
			for (EnumFacing facing : EnumFacing.VALUES) {
				if (providesSignal(facing) && sl[facing.ordinal()] < signalLevel - 1) {
					propagateNotify(facing);
				}
			}

			for (EnumFacing side : EnumFacing.VALUES) {
				WireLocation wside = WireLocation.get(side);

				for (EnumFacing direction : EnumFacing.VALUES) {
					if (direction.getAxis() == side.getAxis()) {
						continue;
					}

					if (connectsCorner(wside, direction) && cl[direction.ordinal() + side.ordinal() * 6] < signalLevel - 1) {
						propagateNotifyCorner(side, direction);
					}
				}
			}
		}
	}

	private void propagateNotifyCorner(EnumFacing side, EnumFacing direction) {
		BlockPos cornerPos = pos.offset(side).offset(direction);
		TileEntity tile = worldObj.getTileEntity(cornerPos);
		if (tile instanceof TileWire) {
			((TileWire) tile).propagate();
		}
	}

	private void propagateNotify(EnumFacing facing) {
		TileEntity nt = getNeighbourTile(facing);
		if (nt instanceof TileWire) {
			((TileWire) nt).propagate();
		} else if (nt instanceof IRedstoneUpdatable) {
			((IRedstoneUpdatable) nt).onRedstoneInputChanged();
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
		tag.setLong("C", cornerConnectionCache);
		return new S35PacketUpdateTileEntity(getPos(), getBlockMetadata(), tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		wireSet = pkt.getNbtCompound().getByte("w");
		connectionCache = pkt.getNbtCompound().getLong("c");
		cornerConnectionCache = pkt.getNbtCompound().getLong("C");
		signalLevel = pkt.getNbtCompound().getShort("s");

		anyConnectionCache = connectionCache | cornerConnectionCache;

		scheduleRenderUpdate();
	}

	public boolean setWire(WireLocation side, boolean b) {
		if (!(b ^ hasWire(side))) {
			return false;
		}

		if (b) {
			if (side != WireLocation.FREESTANDING && !WireUtils.canPlaceWire(worldObj, pos.offset(side.facing()), side.facing().getOpposite())) {
				return false;
			}

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
		if (getWorld() != null && !getWorld().isRemote) {
			scheduledConnectionUpdate = true;
		}
	}

	protected boolean hasWires() {
		return wireSet != 0;
	}

	public boolean hasWire(WireLocation side) {
		return (wireSet & (1 << side.ordinal())) != 0;
	}

	private int getConnectionIndex(WireLocation side, EnumFacing direction) {
		return side.ordinal() * 6 + direction.ordinal();
	}

	private boolean canConnect(WireLocation side, EnumFacing direction) {
		return hasWire(side);
	}

	private boolean connectsInternal(WireLocation side, EnumFacing direction) {
		EnumFacing sideF = side.facing();

		if (sideF != null && sideF.getAxis() == direction.getAxis()) {
			return false;
		}

		if (canConnect(WireLocation.get(direction), sideF)) {
			return true;
		}

		BlockPos connectingPos = pos.offset(direction);
		IBlockState connectingState = worldObj.getBlockState(connectingPos);
		Block connectingBlock = connectingState.getBlock();
		TileEntity connectingTile = getNeighbourTile(direction);

		if (connectingTile instanceof TileWire) {
			if (((TileWire) connectingTile).canConnect(side, direction.getOpposite())) {
				return true;
			}
		} else if (connectingTile instanceof IConnectable) {
			if (((IConnectable) connectingTile).canConnect(this, side, direction.getOpposite())) {
				return true;
			}
		} else {
			if (connectingBlock instanceof BlockRedstoneDiode && side != WireLocation.DOWN) {
				return false;
			}

			if (side == WireLocation.FREESTANDING && !connectingBlock.isSideSolid(worldObj, connectingPos, direction.getOpposite())) {
				return false;
			}

			if (connectingBlock.canConnectRedstone(worldObj, connectingPos, direction.getOpposite())) {
				return true;
			}
		}

		return false;
	}

	private boolean cornerConnectsInternal(WireLocation side, EnumFacing direction) {
		if (!hasWire(side)) {
			return false;
		}

		BlockPos middlePos = pos.offset(direction);
		if (worldObj.isSideSolid(middlePos, direction.getOpposite()) || worldObj.isSideSolid(middlePos, side.facing().getOpposite())) {
			return false;
		}

		BlockPos cornerPos = middlePos.offset(side.facing());
		TileEntity cornerTile = worldObj.getTileEntity(cornerPos);
		if (cornerTile instanceof TileWire && ((TileWire) cornerTile).hasWire(WireLocation.get(direction.getOpposite()))) {
			return true;
		}

		return false;
	}

	public boolean providesSignal(EnumFacing side) {
		return (signalCache & (1 << side.ordinal())) != 0;
 	}

	protected boolean dropWire(WireLocation side, EntityPlayer player) {
		if (setWire(side, false)) {
			if (player == null || !player.capabilities.isCreativeMode) {
				Block.spawnAsEntity(worldObj, pos, new ItemStack(Item.getItemFromBlock(getBlockType()), 1, getItemMetadata(side)));
			}

			return true;
		} else {
			return false;
		}
	}

	private void updateConnections() {
		long oldConnectionCache = connectionCache;
		long oldCornerConnectionCache = cornerConnectionCache;
		connectionCache = 0;
		cornerConnectionCache = 0;
		signalCache = 0;

		for (WireLocation side : WireLocation.VALUES) {
			if (hasWire(side)) {
				if (side != WireLocation.FREESTANDING && !WireUtils.canPlaceWire(worldObj, pos.offset(side.facing()), side.facing().getOpposite())) {
					dropWire(side, null);
					scheduleNeighborUpdate();
					continue;
				}

				for (EnumFacing facing : EnumFacing.VALUES) {
					if (connectsInternal(side, facing)) {
						connectionCache |= (long) 1 << getConnectionIndex(side, facing);
						signalCache |= 1 << facing.ordinal();
					} else if (side != WireLocation.FREESTANDING && cornerConnectsInternal(side, facing)) {
						cornerConnectionCache |= (long) 1 << getConnectionIndex(side, facing);
					}
				}
			}
		}

		if (wireSet == 0) {
			invalidate();
			getBlockType().breakBlock(worldObj, pos, worldObj.getBlockState(pos));
			worldObj.setBlockToAir(pos);
			return;
		}

		if (oldConnectionCache != connectionCache || oldCornerConnectionCache != cornerConnectionCache) {
			anyConnectionCache = connectionCache | cornerConnectionCache;
			scheduleNeighborUpdate();
			schedulePropagationUpdate();
			scheduleRenderUpdate();
		}
	}

	public boolean connects(WireLocation side, EnumFacing direction) {
		return (connectionCache & ((long) 1 << getConnectionIndex(side, direction))) != 0;
	}

	public boolean connectsAny(WireLocation side, EnumFacing direction) {
		return (anyConnectionCache & ((long) 1 << getConnectionIndex(side, direction))) != 0;
	}

	public boolean connectsCorner(WireLocation side, EnumFacing direction) {
		return (cornerConnectionCache & ((long) 1 << getConnectionIndex(side, direction))) != 0;
	}

	// API
	@Override
	public int getSignalStrength(EnumFacing direction) {
		return providesSignal(direction) ? getRedstoneLevel() : 0;
	}

	@Override
	public void onRedstoneInputChanged() {
		schedulePropagationUpdate();
	}

	@Override
	public int getSignalStrength(WireLocation side, EnumFacing direction) {
		return connects(side, direction) ? getRedstoneLevel() : 0;
	}

	@Override
	public boolean wireConnected(WireLocation side, EnumFacing direction) {
		return connects(side, direction);
	}

	@Override
	public boolean wireConnectedCorner(WireLocation side, EnumFacing direction) {
		return connectsCorner(side, direction);
	}
}
