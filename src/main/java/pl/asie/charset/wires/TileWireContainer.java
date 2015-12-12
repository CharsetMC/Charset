package pl.asie.charset.wires;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledUpdatable;
import pl.asie.charset.api.wires.IConnectable;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneUpdatable;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.wires.logic.Wire;
import pl.asie.charset.wires.logic.WireBundled;
import pl.asie.charset.wires.logic.WireInsulated;
import pl.asie.charset.wires.logic.WireNormal;

public class TileWireContainer extends TileEntity implements ITickable, IWire, IBundledEmitter, IBundledUpdatable, IRedstoneEmitter, IRedstoneUpdatable {
	public static final Property PROPERTY = new Property();

	private static class Property implements IUnlistedProperty<TileWireContainer> {
		private Property() {

		}

		@Override
		public String getName() {
			return "wireTile";
		}

		@Override
		public boolean isValid(TileWireContainer value) {
			return true;
		}

		@Override
		public Class<TileWireContainer> getType() {
			return TileWireContainer.class;
		}

		@Override
		public String valueToString(TileWireContainer value) {
			return "!?";
		}
	}

	public WireKind getWireKind(WireFace side) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].type : WireKind.NORMAL;
	}

	public TileEntity getNeighbourTile(EnumFacing side) {
		return side != null ? worldObj.getTileEntity(pos.offset(side)) : null;
	}

	private final Wire[] wires = new Wire[7];
	private boolean scheduledRenderUpdate, scheduledConnectionUpdate, scheduledNeighborUpdate, scheduledPropagationUpdate;

	@SideOnly(Side.CLIENT)
	public int getRenderColor(WireFace loc) {
		return wires[loc.ordinal()].getRenderColor();
	}

	@Override
	public void validate() {
		super.validate();
		scheduleConnectionUpdate();
		schedulePropagationUpdate();
	}

	@Override
	public void update() {
		if (scheduledConnectionUpdate) {
			scheduledConnectionUpdate = false;
			updateConnections();
		}

		if (scheduledNeighborUpdate) {
			scheduledNeighborUpdate = false;
			worldObj.notifyNeighborsRespectDebug(pos, getBlockType());
			for (EnumFacing facing : EnumFacing.VALUES) {
				worldObj.notifyNeighborsOfStateExcept(pos.offset(facing), getBlockType(), facing.getOpposite());
			}
		}

		if (scheduledPropagationUpdate) {
			scheduledPropagationUpdate = false;
			onWireUpdate(null);
		}

		if (scheduledRenderUpdate) {
			scheduledRenderUpdate = false;
			if (!worldObj.isRemote) {
				getWorld().markBlockForUpdate(pos);
			} else {
				getWorld().markBlockRangeForRenderUpdate(pos, pos);
			}
		}
	}

	public boolean canProvideStrongPower(EnumFacing direction) {
		return hasWire(WireFace.get(direction), WireKind.NORMAL);
	}

	public boolean canProvideWeakPower(EnumFacing direction) {
		WireType onFaceType = getWireType(WireFace.get(direction));
		if (onFaceType != null && onFaceType != WireType.NORMAL) {
			return false;
		}

		for (WireFace face : WireFace.VALUES) {
			if (face.facing() == direction.getOpposite()) {
				continue;
			}

			if (hasWire(face)) {
				if (getWireType(face) == WireType.NORMAL) {
					return true;
				}

				if (connects(face, direction)) {
					return true;
				}
			}
		}

		return false;
	}

	public int getItemMetadata(WireFace loc) {
		return ((wires[loc.ordinal()] != null ? wires[loc.ordinal()].type.ordinal() : 0) << 1) | (loc == WireFace.CENTER ? 1 : 0);
	}

	public boolean canConnectInternal(WireFace from, WireFace to) {
		return wires[to.ordinal()] != null && wires[to.ordinal()].type.connects(wires[from.ordinal()].type);
	}

	public boolean canConnectExternal(WireFace from, WireFace to) {
		EnumFacing direction = to.facing();

		BlockPos connectingPos = pos.offset(direction);
		IBlockState connectingState = worldObj.getBlockState(connectingPos);
		Block connectingBlock = connectingState.getBlock();
		TileEntity connectingTile = getNeighbourTile(direction);

		if (connectingTile instanceof TileWireContainer) {
			TileWireContainer wc = (TileWireContainer) connectingTile;
			if (wc.wires[from.ordinal()] != null && wc.wires[from.ordinal()].type.connects(wires[from.ordinal()].type)) {
				return true;
			}
		} else if (connectingTile instanceof IConnectable) {
			IConnectable tc = (IConnectable) connectingTile;
			if (tc.canConnect(getWireType(from), from, direction.getOpposite())) {
				return true;
			}
		} else if (getWireType(from) != WireType.BUNDLED) {
			if ((connectingBlock instanceof BlockRedstoneDiode || connectingBlock instanceof BlockRedstoneWire) && from != WireFace.DOWN) {
				return false;
			}

			if (from == WireFace.CENTER && !connectingBlock.isSideSolid(worldObj, connectingPos, direction.getOpposite())) {
				return false;
			}

			if (connectingBlock.canProvidePower() && connectingBlock.canConnectRedstone(worldObj, connectingPos, direction.getOpposite())) {
				return true;
			}
		}

		return false;
	}

	public int getInsulatedSignalLevel(WireFace side, int i) {
		if (wires[side.ordinal()] != null) {
			switch (wires[side.ordinal()].type.type()) {
				case BUNDLED:
					return wires[side.ordinal()].getBundledSignalLevel(i);
				default:
					return wires[side.ordinal()].getSignalLevel();
			}
		}

		return 0;
	}

	public byte getInsulatedRedstoneLevel(WireFace side, int i) {
		if (wires[side.ordinal()] != null) {
			switch (wires[side.ordinal()].type.type()) {
				case BUNDLED:
					return wires[side.ordinal()].getBundledRedstoneLevel(i);
				default:
					return (byte) wires[side.ordinal()].getRedstoneLevel();
			}
		}

		return 0;
	}

	public int getBundledSignalLevel(WireFace side, int i) {
		if (wires[side.ordinal()] != null) {
			if (wires[side.ordinal()].type.type() == WireType.INSULATED) {
				return wires[side.ordinal()].type.color() == i ? wires[side.ordinal()].getSignalLevel() : 0;
			} else {
				return wires[side.ordinal()].getBundledSignalLevel(i);
			}
		}

		return 0;
	}

	public byte getBundledRedstoneLevel(WireFace side, int i) {
		if (wires[side.ordinal()] != null) {
			if (wires[side.ordinal()].type.type() == WireType.INSULATED) {
				return wires[side.ordinal()].type.color() == i ? (byte) wires[side.ordinal()].getRedstoneLevel() : 0;
			} else {
				return wires[side.ordinal()].getBundledRedstoneLevel(i);
			}
		}

		return 0;
	}


	public int getSignalLevel(WireFace side) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].getSignalLevel() : 0;
	}

	public int getRedstoneLevel(WireFace side) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].getRedstoneLevel() : 0;
	}

	public int getStrongRedstoneLevel(EnumFacing direction) {
		return getRedstoneLevel(WireFace.get(direction));
	}

	public int getWeakRedstoneLevel(EnumFacing direction) {
		int signal = getRedstoneLevel(WireFace.get(direction));

		for (Wire w : wires) {
			if (w != null && (w.connects(direction) || (w.type == WireKind.NORMAL && w.location.facing() != direction.getOpposite()))) {
				signal = Math.max(signal, w.getRedstoneLevel());
			}
		}

		return signal;
	}

	public boolean canConnectCorner(WireFace from, WireFace to) {
		EnumFacing side = from.facing();
		EnumFacing direction = to.facing();

		BlockPos middlePos = pos.offset(direction);
		if (worldObj.isSideSolid(middlePos, direction.getOpposite()) || worldObj.isSideSolid(middlePos, side.getOpposite())) {
			return false;
		}

		BlockPos cornerPos = middlePos.offset(side);
		TileEntity cornerTile = worldObj.getTileEntity(cornerPos);
		if (cornerTile instanceof TileWireContainer) {
			TileWireContainer wc = (TileWireContainer) cornerTile;
			EnumFacing wireSide = direction.getOpposite();

			if (wc.wires[wireSide.ordinal()] != null && wc.wires[wireSide.ordinal()].type.connects(wires[from.ordinal()].type)) {
				return true;
			}
		}

		return false;
	}

	protected boolean dropWire(WireFace side, EntityPlayer player) {
		int wireMeta = getItemMetadata(side);

		if (removeWire(side)) {
			if (player == null || !player.capabilities.isCreativeMode) {
				Block.spawnAsEntity(worldObj, pos, new ItemStack(Item.getItemFromBlock(getBlockType()), 1, wireMeta));
			}

			scheduleConnectionUpdate();
			scheduleRenderUpdate();

			return true;
		} else {
			return false;
		}
	}

	protected boolean hasWires() {
		for (int i = 0; i < wires.length; i++) {
			if (wires[i] != null) {
				return true;
			}
		}

		return false;
	}

	public void updateWireLocation(WireFace loc) {
		if (wires[loc.ordinal()] != null) {
			wires[loc.ordinal()].propagate();
		}
	}

	public void onWireUpdate(EnumFacing side) {
		for (Wire w : wires) {
			if (w != null && (side == null || w.connects(side))) {
				w.propagate();
			}
		}
	}

	private void updateConnections() {
		for (WireFace side : WireFace.VALUES) {
			if (wires[side.ordinal()] != null) {
				if (side != WireFace.CENTER && !WireUtils.canPlaceWire(worldObj, pos.offset(side.facing()), side.facing().getOpposite())) {
					dropWire(side, null);
					scheduleNeighborUpdate();
					continue;
				}

				wires[side.ordinal()].updateConnections();
			}
		}

		if (!hasWires()) {
			invalidate();
			getBlockType().breakBlock(worldObj, pos, worldObj.getBlockState(pos));
			worldObj.setBlockToAir(pos);
			return;
		}
	}

	public boolean providesSignal(EnumFacing direction) {
		for (Wire w : wires) {
			if (w != null && w.connects(direction)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasWire(WireFace side, WireKind type) {
		return wires[side.ordinal()] != null && wires[side.ordinal()].type == type;
	}

	public boolean hasWire(WireFace side) {
		return wires[side.ordinal()] != null;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);

		for (int i = 0; i < 7; i++) {
			wires[i] = null;
			if (tag.hasKey("wire" + i)) {
				NBTTagCompound cpd = tag.getCompoundTag("wire" + i);
				wires[i] = createWire(WireFace.VALUES[i], cpd.getByte("id"));
				wires[i].readFromNBT(cpd);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		writeToNBT(tag, false);
	}

	private void writeToNBT(NBTTagCompound tag, boolean isPacket) {
		super.writeToNBT(tag);

		for (int i = 0; i < 7; i++) {
			if (wires[i] != null) {
				NBTTagCompound cpd = new NBTTagCompound();
				cpd.setByte("id", (byte) wires[i].type.ordinal());
				wires[i].writeToNBT(cpd, isPacket);
				tag.setTag("wire" + i, cpd);
			}
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag, true);
		return new S35PacketUpdateTileEntity(getPos(), getBlockMetadata(), tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
		scheduleRenderUpdate();
	}

	public boolean removeWire(WireFace side) {
		if (!hasWire(side)) {
			return false;
		}

		this.wires[side.ordinal()] = null;
		scheduleConnectionUpdate();
		scheduleRenderUpdate();

		return true;
	}

	public boolean addWire(WireFace side, int meta) {
		if (hasWire(side)) {
			return false;
		}

		if (side != WireFace.CENTER && !WireUtils.canPlaceWire(worldObj, pos.offset(side.facing()), side.facing().getOpposite())) {
			return false;
		}

		this.wires[side.ordinal()] = createWire(side, meta >> 1);

		scheduleConnectionUpdate();
		scheduleRenderUpdate();

		return true;
	}

	private Wire createWire(WireFace side, int meta) {
		if (meta >= 1 && meta < 17) {
			return new WireInsulated(WireKind.insulated(meta - 1), side, this);
		} else if (meta == 17) {
			return new WireBundled(WireKind.BUNDLED, side, this);
		} else {
			return new WireNormal(WireKind.NORMAL, side, this);
		}
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

	public boolean connects(WireFace side, EnumFacing direction) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].connects(direction) : null;
	}

	public boolean connectsAny(WireFace side, EnumFacing direction) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].connectsAny(direction) : null;
	}

	public boolean connectsCorner(WireFace side, EnumFacing direction) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].connectsCorner(direction) : null;
	}

	// API
	@Override
	public WireType getWireType(WireFace location) {
		return hasWire(location) ? wires[location.ordinal()].type.type() : null;
	}

	@Override
	public int getInsulatedColor(WireFace location) {
		return getWireType(location) == WireType.INSULATED ? wires[location.ordinal()].type.color() : -1;
	}

	@Override
	public byte[] getBundledSignal(WireFace face, EnumFacing toDirection) {
		Wire wire = wires[face.ordinal()];
		return wire instanceof WireBundled ? ((WireBundled) wire).getBundledSignal() : null;
	}

	@Override
	public void onBundledInputChanged(EnumFacing face) {
		schedulePropagationUpdate();
	}

	@Override
	public int getRedstoneSignal(WireFace face, EnumFacing toDirection) {
		return connects(face, toDirection) ? getRedstoneLevel(face) : 0;
	}

	@Override
	public void onRedstoneInputChanged(EnumFacing face) {
		schedulePropagationUpdate();
	}
}
