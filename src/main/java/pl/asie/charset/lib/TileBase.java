/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileBase extends TileEntity {
	private boolean initialized = false;

	protected void initialize() {

	}

	public boolean hasDataPacket() {
		return true;
	}

	public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {

	}

	public int getComparatorValue() {
		return 0;
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

	public void dropContents() {

	}
}
