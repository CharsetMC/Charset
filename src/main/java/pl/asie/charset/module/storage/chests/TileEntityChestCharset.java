/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.storage.chests;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.lib.IMultiblockStructure;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.block.TraitLockable;
import pl.asie.charset.lib.block.TraitMaterial;
import pl.asie.charset.lib.block.TraitNameable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.inventory.GuiHandlerCharset;
import pl.asie.charset.lib.inventory.IContainerHandler;
import pl.asie.charset.lib.utils.MathUtils;
import pl.asie.charset.lib.utils.MultipartUtils;
import pl.asie.charset.lib.utils.redstone.RedstoneUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TileEntityChestCharset extends TileBase implements IContainerHandler, IItemHandlerModifiable, IMultiblockStructure, IDebuggable, ITickable {
	protected TraitMaterial material;
	protected final TraitLockable lockable;
	private final ItemStackHandler stacks = new ItemStackHandler(27) {
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			TileEntityChestCharset.this.markChunkDirty();
			TileEntityChestCharset.this.updateComparators();
		}
	};
	private TileEntityChestCharset neighbor;
	private EnumFacing neighborFace;
	private float lidAngle, prevLidAngle;

	private LongSet playerIds = new LongOpenHashSet();
	private int playerCountClient;

	public TileEntityChestCharset() {
		registerTrait("wood", material = new TraitMaterial("wood", ItemMaterialRegistry.INSTANCE.getDefaultMaterialByType("plank")));
		registerTrait("lock", lockable = new TraitLockable(this));
		registerTrait("name", new TraitNameable());
	}

	protected int getPlayerCount() {
		return (world != null && !world.isRemote) ? playerIds.size() : playerCountClient;
	}

	protected float getLidAngle(float partialTicks) {
		if (hasNeighbor() && neighborFace.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
			return MathUtils.interpolate(getNeighbor().prevLidAngle, getNeighbor().lidAngle, partialTicks);
		}
		return MathUtils.interpolate(prevLidAngle, lidAngle, partialTicks);
	}

	public TileEntityChestCharset getNeighbor() {
		if (neighbor == null || neighbor.isInvalid()) {
			neighbor = null;

			if (neighborFace != null) {
				if (!world.isRemote) {
					// Check if neighbor still exists

					//noinspection ConstantConditions
					if (world != null && pos != null) {
						TileEntity tile = world.getTileEntity(pos.offset(neighborFace));
						if (tile instanceof TileEntityChestCharset && !tile.isInvalid()) {
							setNeighbor((TileEntityChestCharset) tile, neighborFace);
						} else {
							setNeighbor(null, null);
						}
					}
				} else {
					//noinspection ConstantConditions
					if (world != null && pos != null) {
						TileEntity tile = world.getTileEntity(pos.offset(neighborFace));
						if (tile instanceof TileEntityChestCharset && !tile.isInvalid()) {
							neighbor = (TileEntityChestCharset) tile;
						}
					}
				}
			}
		}

		return neighbor;
	}

	public EnumFacing getNeighborFace() {
		return neighborFace;
	}

	public boolean hasNeighbor() {
		return neighborFace != null && getNeighbor() != null;
	}

	@Override
	public int getComparatorValue(int max) {
		return RedstoneUtils.getComparatorValue(this, max);
	}

	@Override
	public void update() {
		super.update();

		this.prevLidAngle = this.lidAngle;
		int pc = getPlayerCount();
		if (pc > 0) {
			lidAngle = Math.min(1.0F, lidAngle + (hasNeighbor() ? 0.08F : 0.1F));
		} else {
			lidAngle = Math.max(0.0F, lidAngle - (hasNeighbor() ? 0.08F : 0.1F));
		}
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		super.readNBTData(compound, isClient);

		if (!isClient && compound.hasKey("inv")) {
			stacks.deserializeNBT(compound.getCompoundTag("inv"));
		}

		EnumFacing oldNF = neighborFace;
		neighborFace = compound.hasKey("nf", Constants.NBT.TAG_ANY_NUMERIC) ? EnumFacing.byIndex(compound.getByte("nf")) : null;

		if (oldNF != neighborFace && isClient) {
			markBlockForRenderUpdate();
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound compound, boolean isClient) {
		compound = super.writeNBTData(compound, isClient);
		if (!isClient) {
			NBTTagCompound invTag = stacks.serializeNBT();
			compound.setTag("inv", invTag);
		}

		if (neighborFace != null) {
			compound.setByte("nf", (byte) neighborFace.ordinal());
		}
		return compound;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> stackList, IBlockState state, int fortune, boolean silkTouch) {
		stackList.add(getDroppedBlock(state));
		for (int i = 0; i < stacks.getSlots(); i++) {
			ItemStack s = stacks.getStackInSlot(i);
			if (!s.isEmpty()) {
				stackList.add(s);
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == Capabilities.MULTIBLOCK_STRUCTURE
				|| capability == Capabilities.DEBUGGABLE) {
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
		}

		if (capability == Capabilities.MULTIBLOCK_STRUCTURE) {
			return Capabilities.MULTIBLOCK_STRUCTURE.cast(this);
		}

		if (capability == Capabilities.DEBUGGABLE) {
			return Capabilities.DEBUGGABLE.cast(this);
		}

		return super.getCapability(capability, facing);
	}

	public boolean isBlocked() {
		// self position check
		if (MultipartUtils.INSTANCE.intersects(
				Collections.singleton(
						new AxisAlignedBB(0, 0.875, 0, 1, 1, 1)
				), world, pos, (s) -> !(s.getBlock() instanceof BlockChestCharset)
		)) {
			return true;
		}

		// position check
		BlockPos posUp = pos.up();
		if (world.getBlockState(posUp).doesSideBlockChestOpening(world, posUp, EnumFacing.DOWN)) {
			return true;
		}

		// revolver ocelot check
		for (EntityOcelot ocelot : world.getEntitiesWithinAABB(EntityOcelot.class, new AxisAlignedBB(pos.add(0, 1, 0), pos.add(1, 2, 1)))) {
			if (ocelot.isSitting()) {
				return true;
			}
		}

		return false;
	}

	public boolean activate(EntityPlayer player, EnumFacing side, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!stack.isEmpty()) {
			if (stack.getItem().getToolClasses(stack).contains("wrench")) {
				boolean attached = false;

				BlockPos otherPos = pos.offset(side);
				TileEntity otherTile = world.getTileEntity(otherPos);
				if (otherTile instanceof TileEntityChestCharset
						&& ((TileEntityChestCharset) otherTile).material.getMaterial() == material.getMaterial()) {
					if (!((TileEntityChestCharset) otherTile).hasNeighbor()) {
						attached = true;
						setNeighbor((TileEntityChestCharset) otherTile, side);
						player.swingArm(hand);
					}
				}

				if (hasNeighbor() && !attached) {
					neighbor.setNeighbor(null, null);
					setNeighbor(null, null);
					player.swingArm(hand);
				}

				return true;
			}
		}

		if (isBlocked() || (hasNeighbor() && getNeighbor().isBlocked())) {
			return true;
		}

		if (!world.isRemote) {
			player.openGui(ModCharset.instance, GuiHandlerCharset.CHEST, getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
			player.addStat(StatList.CHEST_OPENED);
		}
		return true;
	}

	private IItemHandlerModifiable ihFirstHandler() {
		return neighborFace != null && neighborFace.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? neighbor.stacks : this.stacks;
	}

	private IItemHandlerModifiable ihSecondHandler() {
		return neighborFace != null && neighborFace.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? neighbor.stacks : this.stacks;
	}

	private int ihSlot(int slot) {
		if (slot >= ihFirstHandler().getSlots()) {
			return slot - ihFirstHandler().getSlots();
		} else {
			return slot;
		}
	}

	private IItemHandlerModifiable ihHandler(int slot) {
		if (slot >= ihFirstHandler().getSlots()) {
			return ihSecondHandler();
		} else {
			return ihFirstHandler();
		}
	}

	@Nullable
	@Override
	public ITextComponent getDisplayName() {
		return getTraitDisplayName(
				new TextComponentTranslation(hasNeighbor() ? "container.chestDouble" : "container.chest")
		);
	}

	@Override
	public int getSlots() {
		return hasNeighbor() ? neighbor.stacks.getSlots() + stacks.getSlots() : stacks.getSlots();
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return ihHandler(slot).getStackInSlot(ihSlot(slot));
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		return ihHandler(slot).insertItem(ihSlot(slot), stack, simulate);
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ihHandler(slot).extractItem(ihSlot(slot), amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return ihHandler(slot).getSlotLimit(ihSlot(slot));
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		ihHandler(slot).setStackInSlot(ihSlot(slot), stack);
	}

	@Override
	public Iterator<BlockPos> iterator() {
		return hasNeighbor() ? ImmutableList.of(this.pos, neighbor.pos).iterator() : ImmutableList.of(this.pos).iterator();
	}

	@Override
	public boolean contains(BlockPos pos) {
		if (pos.equals(this.pos)) {
			return true;
		}

		if (hasNeighbor() && getNeighbor().pos.equals(pos)) {
			return true;
		}

		return false;
	}

	protected void setNeighbor(TileEntityChestCharset tile, EnumFacing facing) {
		this.neighbor = tile;
		this.neighborFace = facing;
		markBlockForUpdate();
		markDirty();

		if (tile != null) {
			tile.neighbor = this;
			tile.neighborFace = facing.getOpposite();
			tile.markBlockForUpdate();
			tile.markDirty();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (hasNeighbor()) {
			return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
		} else {
			return new AxisAlignedBB(pos.add(0, 0, 0), pos.add(1, 2, 1));
		}
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void addDebugInformation(List<String> stringList, Side side) {
		getNeighbor();
		stringList.add("Neighbor: " + (neighborFace != null ? neighborFace.name() : "none"));
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		switch (id) {
			case 1:
				this.playerCountClient = type;
				return true;
			default:
				return super.receiveClientEvent(id, type);
		}
	}

	private void playChestSound(SoundEvent event) {
		float pitch = this.world.rand.nextFloat() * 0.1F + 0.9F;
		Vec3d pos = new Vec3d(getPos()).add(0.5, 0.5, 0.5);
		if (hasNeighbor()) {
			pos = pos.add(
					neighborFace.getXOffset() * 0.5,
					neighborFace.getYOffset() * 0.5,
					neighborFace.getZOffset() * 0.5
			);
			pitch *= 0.85F;
		}

		world.playSound(null, pos.x, pos.y, pos.z, event, SoundCategory.BLOCKS, 0.5F, pitch);
	}

	@Override
	public void onOpenedBy(EntityPlayer player) {
		onOpenedByInner(player);
		if (hasNeighbor()) {
			getNeighbor().onOpenedByInner(player);
		}
	}

	@Override
	public void onClosedBy(EntityPlayer player) {
		onClosedByInner(player);
		if (hasNeighbor()) {
			getNeighbor().onClosedByInner(player);
		}
	}

	protected void onOpenedByInner(EntityPlayer player) {
		if (!player.isSpectator()) {
			if (!playerIds.add(player.getEntityId())) {
				return;
			}

			if (!hasNeighbor() || neighborFace.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
				if (playerIds.size() == 1) {
					playChestSound(SoundEvents.BLOCK_CHEST_OPEN);
				}
			}

			world.addBlockEvent(pos, getBlockType(), 1, playerIds.size());
		}
	}

	protected void onClosedByInner(EntityPlayer player) {
		if (!player.isSpectator()) {
			if (!playerIds.remove(player.getEntityId())) {
				return;
			}

			if (!hasNeighbor() || neighborFace.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
				if (playerIds.size() == 0) {
					playChestSound(SoundEvents.BLOCK_CHEST_CLOSE);
				}
			}

			world.addBlockEvent(pos, getBlockType(), 1, playerIds.size());
		}
	}

	@Override
	public boolean canRenderBreaking() {
		return true;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return world.getTileEntity(pos) == this && player.getDistanceSq(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
	}
}
