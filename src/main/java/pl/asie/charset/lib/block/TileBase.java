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

package pl.asie.charset.lib.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileBase extends TileEntity {
	public ItemStack getPickedBlock() {
		return getDroppedBlock();
	}

	public ItemStack getDroppedBlock() {
		return new ItemStack(getBlockType());
	}

	public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {

	}

	public int getComparatorValue() {
		return 0;
	}

	public boolean hasDataPacket() {
		return true;
	}

	public void readNBTData(NBTTagCompound compound, boolean isClient) {

	}

	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		return compound;
	}

	@Override
	public final SPacketUpdateTileEntity getUpdatePacket() {
		return hasDataPacket() ? new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), writeNBTData(new NBTTagCompound(), true)) : null;
	}

	@Override
	public final NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = super.writeToNBT(new NBTTagCompound());
		compound = writeNBTData(compound, true);
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
		readNBTData(compound, (world instanceof WorldClient));
	}

	@Override
	public final NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		return writeNBTData(compound, false);
	}

	public void update() {
	}

	public TileEntity getNeighbourTile(EnumFacing side) {
		return world != null && side != null ? world.getTileEntity(pos.offset(side)) : null;
	}

	public void markBlockForRenderUpdate() {
		world.markBlockRangeForRenderUpdate(pos, pos);
	}

	public void markBlockForUpdate() {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
	}
}
