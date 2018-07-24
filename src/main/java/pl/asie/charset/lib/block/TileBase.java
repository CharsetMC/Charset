/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.lib.capability.Capabilities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TileBase extends TileEntity {
	public enum InvalidationType {
		REMOVAL,
		UNLOAD
	};

	private Map<String, Trait> traits;
	private int lastComparatorValue = -1;

	public TileBase() {
		super();
		if (traits == null) {
			traits = new LinkedHashMap<>();
		}
	}

	public final void registerTrait(String s, Trait t) {
		if (traits == null) {
			traits = new LinkedHashMap<>();
		}
		traits.put(s, t);
	}

	protected final boolean updateComparators() {
		int cc = getComparatorValue(15);
		if (cc != lastComparatorValue) {
			world.updateComparatorOutputLevel(getPos(), getBlockType());
			lastComparatorValue = cc;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onLoad() {
		for (Trait t : traits.values()) {
			t.onLoad();
		}
	}

	public void invalidate(InvalidationType type) {
		for (Trait t : traits.values()) {
			t.onInvalidate(type);
		}
	}

	@Override
	public final void invalidate() {
		super.invalidate();
		invalidate(InvalidationType.REMOVAL);
	}

	@Override
	public final void onChunkUnload() {
		invalidate(InvalidationType.UNLOAD);
	}

	public void getDrops(NonNullList<ItemStack> stacks, IBlockState state, int fortune, boolean silkTouch) {
		stacks.add(getDroppedBlock(state));
	}

	public ItemStack getPickedBlock(@Nullable EntityPlayer player, @Nullable RayTraceResult result, IBlockState state) {
		return getDroppedBlock(state);
	}

	public ItemStack getDroppedBlock(IBlockState state) {
		ItemStack stack = new ItemStack(state.getBlock());;
		saveToStack(stack);
		return stack;
	}

	protected final ITextComponent getTraitDisplayName(ITextComponent fallback) {
		for (Trait t : traits.values()) {
			if (t instanceof TraitNameable) {
				return ((TraitNameable) t).getName(fallback);
			}
		}
		return fallback;
	}

	@Nullable
	@Override
	public ITextComponent getDisplayName() {
		return getTraitDisplayName(null);
	}

	public void loadFromStack(ItemStack stack) {
		for (Trait t : traits.values()) {
			if (t instanceof ITraitItemAppendable) {
				((ITraitItemAppendable) t).loadFromStack(stack);
			}
		}
	}

	public ItemStack saveToStack(ItemStack stack) {
		for (Trait t : traits.values()) {
			if (t instanceof ITraitItemAppendable) {
				stack = ((ITraitItemAppendable) t).saveToStack(stack);
			}
		}
		return stack;
	}

	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		loadFromStack(stack);
	}

	public int getComparatorValue(int max) {
		return 0;
	}

	public boolean hasDataPacket() {
		return true;
	}

	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		for (Map.Entry<String, Trait> entry : traits.entrySet()) {
			if (compound.hasKey(entry.getKey(), Constants.NBT.TAG_COMPOUND)) {
				entry.getValue().readNBTData(compound.getCompoundTag(entry.getKey()), isClient);
			}
		}
	}

	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		for (Map.Entry<String, Trait> entry : traits.entrySet()) {
			NBTTagCompound traitCpd = entry.getValue().writeNBTData(isClient);
			if (traitCpd != null) {
				compound.setTag(entry.getKey(), traitCpd);
			}
		}
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
		readNBTData(compound, world != null && world.isRemote);
	}

	@Override
	public final NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		return writeNBTData(compound, false);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		for (Trait t : traits.values()) {
			if (t.hasCapability(capability, facing)) {
				return true;
			}
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		for (Trait t : traits.values()) {
			if (t.hasCapability(capability, facing)) {
				return t.getCapability(capability, facing);
			}
		}

		return super.getCapability(capability, facing);
	}

	@Override
	protected void setWorldCreate(World worldIn) {
		// MCP mappings are sometimes really silly.
		setWorld(worldIn);
	}

	@Override
	public void rotate(Rotation rotationIn) {
		if (this.hasCapability(Capabilities.AXIS_ROTATABLE, null)) {
			IAxisRotatable rotatable = this.getCapability(Capabilities.AXIS_ROTATABLE, null);
			int count = 0;
			switch (rotationIn) {
				case CLOCKWISE_90:
					count = 1;
					break;
				case CLOCKWISE_180:
					count = 2;
					break;
				case COUNTERCLOCKWISE_90:
					count = 3;
					break;
			}

			for (int i = 0; i < count; i++) {
				rotatable.rotateAround(EnumFacing.UP, false);
			}
		}
	}

	public void update() {
	}

	@Override
	public boolean canRenderBreaking() {
		return false;
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

	public void markChunkDirty() {
		world.markChunkDirty(pos, this);
	}
}
