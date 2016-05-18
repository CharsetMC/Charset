package pl.asie.charset.lib;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

/**
 * Created by asie on 11/7/15.
 */
public class TileBase extends TileEntity {
	private boolean initialized = false;

	protected void initialize() {

	}

	public boolean hasDataPacket() {
		return true;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return hasDataPacket() ? new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), writeNBTData(new NBTTagCompound(), true)) : null;
	}

	@Override
	public final NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = super.writeToNBT(new NBTTagCompound());
		writeNBTData(compound, true);
		return compound;
	}

	@Override
	public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		if (pkt != null && pkt.getNbtCompound() != null) {
			readNBTData(pkt.getNbtCompound(), true);
		}
	}

	@Override
	public final void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readNBTData(compound, false);
	}

	@Override
	public final NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		return writeNBTData(compound, false);
	}

	public void readNBTData(NBTTagCompound compound, boolean isClient) {

	}

	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		return compound;
	}

	public void update() {
		if (!initialized) {
			initialize();
			initialized = true;
		}
	}

	public TileEntity getNeighbourTile(EnumFacing side) {
		return worldObj != null && side != null ? worldObj.getTileEntity(pos.offset(side)) : null;
	}

	public void markBlockForUpdate() {
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(pos, state, state, 3);
	}
}
