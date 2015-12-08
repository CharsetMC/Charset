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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.wires.internal.IRedstoneWire;
import pl.asie.charset.wires.internal.WireLocation;
import pl.asie.charset.wires.logic.Wire;
import pl.asie.charset.wires.logic.WireBundled;
import pl.asie.charset.wires.logic.WireInsulated;
import pl.asie.charset.wires.logic.WireNormal;

public class TileWireContainer extends TileEntity implements ITickable, IRedstoneWire {
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

	public WireType getWireType(WireLocation side) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].type : WireType.NORMAL;
	}

	public TileEntity getNeighbourTile(EnumFacing side) {
		return side != null ? worldObj.getTileEntity(pos.offset(side)) : null;
	}

	private final Wire[] wires = new Wire[7];
	private boolean scheduledRenderUpdate, scheduledConnectionUpdate, scheduledNeighborUpdate, scheduledPropagationUpdate;

	@SideOnly(Side.CLIENT)
	public int getRenderColor(WireLocation loc) {
		return wires[loc.ordinal()].getRenderColor();
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
			onWireUpdate(null);
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
		return hasWire(WireLocation.get(direction), WireType.NORMAL);
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
		return ((wires[loc.ordinal()] != null ? wires[loc.ordinal()].type.ordinal() : 0) << 1) | (loc == WireLocation.FREESTANDING ? 1 : 0);
	}

	public boolean canConnectInternal(WireLocation from, WireLocation to) {
		return wires[to.ordinal()] != null && wires[to.ordinal()].type.connects(wires[from.ordinal()].type);
	}

	public boolean canConnectExternal(WireLocation from, WireLocation to) {
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
		} else {
			if (connectingBlock instanceof BlockRedstoneDiode && from != WireLocation.DOWN) {
				return false;
			}

			if (from == WireLocation.FREESTANDING && !connectingBlock.isSideSolid(worldObj, connectingPos, direction.getOpposite())) {
				return false;
			}

			if (connectingBlock.canConnectRedstone(worldObj, connectingPos, direction.getOpposite())) {
				return true;
			}
		}

		return false;
	}

	public int getInsulatedSignalLevel(WireLocation side, int i) {
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
	public int getBundledSignalLevel(WireLocation side, int i) {
		if (wires[side.ordinal()] != null) {
			if (wires[side.ordinal()].type.type() == WireType.Type.INSULATED) {
				return wires[side.ordinal()].type.color() == i ? wires[side.ordinal()].getSignalLevel() : 0;
			} else {
				return wires[side.ordinal()].getBundledSignalLevel(i);
			}
		}

		return 0;
	}

	public int getSignalLevel(WireLocation side) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].getSignalLevel() : 0;
	}

	public int getRedstoneLevel(WireLocation side) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].getRedstoneLevel() : 0;
	}

	public int getRedstoneLevel(EnumFacing direction) {
		int signal = 0;

		for (Wire w : wires) {
			if (w != null && w.connects(direction)) {
				signal = Math.max(signal, w.getRedstoneLevel());
			}
		}

		return signal;
	}

	public boolean canConnectCorner(WireLocation from, WireLocation to) {
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

	protected boolean dropWire(WireLocation side, EntityPlayer player) {
		if (removeWire(side)) {
			if (player == null || !player.capabilities.isCreativeMode) {
				Block.spawnAsEntity(worldObj, pos, new ItemStack(Item.getItemFromBlock(getBlockType()), 1, getItemMetadata(side)));
			}

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

	public void updateWireLocation(WireLocation loc) {
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
		for (WireLocation side : WireLocation.VALUES) {
			if (wires[side.ordinal()] != null) {
				if (side != WireLocation.FREESTANDING && !WireUtils.canPlaceWire(worldObj, pos.offset(side.facing()), side.facing().getOpposite())) {
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

	public boolean hasWire(WireLocation side, WireType type) {
		return wires[side.ordinal()] != null && wires[side.ordinal()].type == type;
	}

	public boolean hasWire(WireLocation side) {
		return wires[side.ordinal()] != null;
	}



	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);

		for (int i = 0; i < 7; i++) {
			if (tag.hasKey("wire" + i)) {
				NBTTagCompound cpd = tag.getCompoundTag("wire" + i);
				wires[i] = createWire(WireLocation.VALUES[i], cpd.getByte("id"));
				wires[i].readFromNBT(cpd);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		for (int i = 0; i < 7; i++) {
			if (wires[i] != null) {
				NBTTagCompound cpd = new NBTTagCompound();
				cpd.setByte("id", (byte) wires[i].type.ordinal());
				wires[i].writeToNBT(cpd);
				tag.setTag("wire" + i, cpd);
			}
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(getPos(), getBlockMetadata(), tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
		scheduleRenderUpdate();
	}

	public boolean removeWire(WireLocation side) {
		if (!hasWire(side)) {
			return false;
		}

		this.wires[side.ordinal()] = null;
		scheduleConnectionUpdate();
		scheduleRenderUpdate();

		return true;
	}

	public boolean addWire(WireLocation side, int meta) {
		if (hasWire(side)) {
			return false;
		}

		if (side != WireLocation.FREESTANDING && !WireUtils.canPlaceWire(worldObj, pos.offset(side.facing()), side.facing().getOpposite())) {
			return false;
		}

		this.wires[side.ordinal()] = createWire(side, meta >> 1);

		scheduleConnectionUpdate();
		scheduleRenderUpdate();

		return true;
	}

	private Wire createWire(WireLocation side, int meta) {
		if (meta >= 1 && meta < 17) {
			return new WireInsulated(WireType.insulated(meta - 1), side, this);
		} else if (meta == 17) {
			return new WireBundled(WireType.BUNDLED, side, this);
		} else {
			return new WireNormal(WireType.NORMAL, side, this);
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

	public boolean connects(WireLocation side, EnumFacing direction) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].connects(direction) : null;
	}

	public boolean connectsAny(WireLocation side, EnumFacing direction) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].connectsAny(direction) : null;
	}

	public boolean connectsCorner(WireLocation side, EnumFacing direction) {
		return wires[side.ordinal()] != null ? wires[side.ordinal()].connectsCorner(direction) : null;
	}

	// API
	@Override
	public int getSignalStrength(EnumFacing direction) {
		return providesSignal(direction) ? getRedstoneLevel(direction) : 0;
	}

	@Override
	public void onRedstoneInputChanged() {
		schedulePropagationUpdate();
	}

	@Override
	public int getSignalStrength(WireLocation side, EnumFacing direction) {
		return connects(side, direction) ? getRedstoneLevel(side) : 0;
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
