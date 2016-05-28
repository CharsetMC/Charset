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

package pl.asie.charset.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fmp.ForgeMultipartModContainer;
import net.minecraftforge.fmp.client.multipart.AdvancedParticleManager;
import net.minecraftforge.fmp.multipart.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import net.minecraftforge.fmp.microblock.IMicroblock;
import pl.asie.charset.api.pipes.IPipe;
import pl.asie.charset.api.pipes.IShifter;
import pl.asie.charset.lib.IConnectable;
import pl.asie.charset.lib.utils.RotationUtils;

public class PartPipe extends Multipart implements IConnectable, ISlottedPart, INormallyOccludingPart, IPipe, ITickable {
	public static final Property PROPERTY = new Property();

	private static class Property implements IUnlistedProperty<PartPipe> {
		private Property() {

		}

		@Override
		public String getName() {
			return "pipeTile";
		}

		@Override
		public boolean isValid(PartPipe value) {
			return true;
		}

		@Override
		public Class<PartPipe> getType() {
			return PartPipe.class;
		}

		@Override
		public String valueToString(PartPipe value) {
			return "!?";
		}
	}


	private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[7];

	final PipeFluidContainer fluid = new PipeFluidContainer(this);
	boolean renderFast = false;

	protected int[] shifterDistance = new int[6];

	private final Set<PipeItem> itemSet = new HashSet<PipeItem>();
	private byte connectionCache = 0;
	private boolean neighborBlockChanged;
	private boolean requestUpdate;

	static {
		BOXES[6] = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
		for (int i = 0; i < 6; i++) {
			BOXES[i] = RotationUtils.rotateFace(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.25, 0.75), EnumFacing.getFront(i));
		}
	}

	public PartPipe() {
	}

	public void handlePacket(ByteBuf buf) {
		byte oldCC = connectionCache;
		connectionCache = buf.readByte();
		requestUpdate = true;

		if (oldCC != connectionCache) {
			markRenderUpdate();
		}
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);

		if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
			final ByteBuf buf2 = Unpooled.copiedBuffer(buf);

			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					handlePacket(buf2);
				}
			});
		} else {
			handlePacket(buf);
		}
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		buf.writeByte(connectionCache);
	}

	// Block logic

	@Override
	public ItemStack getPickPart(EntityPlayer player, RayTraceResult hit) {
		return new ItemStack(ModCharsetPipes.itemPipe);
	}

	@Override
	public List<ItemStack> getDrops() {
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(new ItemStack(ModCharsetPipes.itemPipe));

		for (PipeItem i : itemSet) {
			if (i.isValid()) {
				drops.add(i.getStack());
			}
		}

		return drops;
	}

	@Override
	public float getHardness(RayTraceResult hit) {
		return 0.3F;
	}

	@Override
	public Material getMaterial() {
		return Material.GLASS;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		addSelectionBoxes(list);
		return list.get(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(AdvancedParticleManager advancedEffectRenderer) {
		advancedEffectRenderer.addBlockDestroyEffects(getPos(),
				Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(getExtendedState(MultipartRegistry.getDefaultState(this).getBaseState())));
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(RayTraceResult partMOP, AdvancedParticleManager advancedEffectRenderer) {
		return true;
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		list.add(new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75));
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(BOXES[6]);
		for (EnumFacing f : EnumFacing.VALUES) {
			if (connects(f)) {
				list.add(BOXES[f.ordinal()]);
			}
		}
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		if (BOXES[6].intersectsWith(mask)) {
			list.add(BOXES[6]);
		}
		for (EnumFacing f : EnumFacing.VALUES) {
			if (connects(f)) {
				if (BOXES[f.ordinal()].intersectsWith(mask)) {
					list.add(BOXES[f.ordinal()]);
				}
			}
		}
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	@Override
	public ResourceLocation getModelPath() {
		return new ResourceLocation("charsetpipes:pipe");
	}

	// Tile logic

	public TileEntity getNeighbourTile(EnumFacing side) {
		return side != null ? getWorld().getTileEntity(getPos().offset(side)) : null;
	}

	private boolean internalConnects(EnumFacing side) {
		ISlottedPart part = getContainer().getPartInSlot(PartSlot.getFaceSlot(side));
		if (part instanceof IMicroblock.IFaceMicroblock) {
			if (!((IMicroblock.IFaceMicroblock) part).isFaceHollow()) {
				return false;
			}
		}

		if (!OcclusionHelper.occlusionTest(OcclusionHelper.boxes(BOXES[side.ordinal()]), p -> p == this, getContainer())) {
			return false;
		}

		if (PipeUtils.getPipe(getWorld(), getPos().offset(side), side.getOpposite()) != null) {
			return true;
		}

		TileEntity tile = getNeighbourTile(side);

		if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) {
			return true;
		}

		if (tile instanceof ISidedInventory || tile instanceof IInventory) {
			return true;
		}

		if (tile instanceof IFluidHandler) {
			return true;
		}

		if (tile != null && tile.hasCapability(ModCharsetPipes.CAP_SHIFTER, side.getOpposite())) {
			return tile.getCapability(ModCharsetPipes.CAP_SHIFTER, side.getOpposite()).getMode() == IShifter.Mode.Extract;
		}

		return false;
	}

	private void updateConnections(EnumFacing side) {
		if (side != null) {
			connectionCache &= ~(1 << side.ordinal());

			if (internalConnects(side)) {
				PartPipe pipe = PipeUtils.getPipe(getWorld(), getPos().offset(side), side.getOpposite());
				if (pipe != null && !pipe.internalConnects(side.getOpposite())) {
					return;
				}

				connectionCache |= 1 << side.ordinal();
			}
		} else {
			for (EnumFacing facing : EnumFacing.VALUES) {
				updateConnections(facing);
			}
		}
	}

	@Override
	public boolean connects(EnumFacing side) {
		return (connectionCache & (1 << side.ordinal())) != 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		readItems(nbt);

		NBTTagCompound tag = nbt.getCompoundTag("fluid");
		fluid.readFromNBT(tag);

		connectionCache = nbt.getByte("cc");
		shifterDistance = nbt.getIntArray("shifterDist");
		if (shifterDistance == null || shifterDistance.length != 6) {
			shifterDistance = new int[6];
		}
	}

	private void readItems(NBTTagCompound nbt) {
		NBTTagList list = nbt.getTagList("items", 10);
		synchronized (itemSet) {
			itemSet.clear();

			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound compound = list.getCompoundTagAt(i);
				PipeItem pipeItem = new PipeItem(this, compound);
				if (pipeItem.isValid()) {
					itemSet.add(pipeItem);
				}
			}
		}
	}

	private void writeItems(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();

		synchronized (itemSet) {
			for (PipeItem i : itemSet) {
				NBTTagCompound cpd = new NBTTagCompound();
				i.writeToNBT(cpd);
				list.appendTag(cpd);
			}
		}

		nbt.setTag("items", list);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeItems(nbt);

		NBTTagCompound tag = new NBTTagCompound();
		fluid.writeToNBT(tag);
		if (tag.getSize() > 0) {
			nbt.setTag("fluid", tag);
		}

		nbt.setByte("cc", connectionCache);
		if (shifterDistance != null) {
			nbt.setIntArray("shifterDist", shifterDistance);
		}
		return nbt;
	}

	@Override
	public void onUnloaded() {
		super.onUnloaded();
		if (getWorld().isRemote) {
			synchronized (itemSet) {
				itemSet.clear();
			}
		}
	}

	@Override
	public void onAdded() {
		updateNeighborInfo(false);
		onLoaded();
		scheduleRenderUpdate();
	}

	@Override
	public void onLoaded() {
		neighborBlockChanged = true;
	}

	@Override
	public void update() {
		if (requestUpdate && getWorld() != null) {
			ModCharsetPipes.packet.sendToServer(new PacketPipeSyncRequest(this));
		}

		if (neighborBlockChanged) {
			updateNeighborInfo(true);
			neighborBlockChanged = false;
		}

		fluid.update();

		synchronized (itemSet) {
			Iterator<PipeItem> itemIterator = itemSet.iterator();
			while (itemIterator.hasNext()) {
				PipeItem p = itemIterator.next();
				if (!p.move()) {
					itemIterator.remove();
				}
			}
		}
	}

	protected int getShifterStrength(EnumFacing direction) {
		return direction == null ? 0 : shifterDistance[direction.ordinal()];
	}

	private void updateShifterSide(EnumFacing dir) {
		int i = dir.ordinal();
		int oldDistance = shifterDistance[i];

		if (shifterDistance[i] == 1 && getNearestShifterInternal(dir) != null) {
			return;
		}

		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(getPos());
		EnumFacing dirO = dir.getOpposite();
		int dist = 0;
		PartPipe pipe;
		TileEntity tile;

		while ((pipe = PipeUtils.getPipe(getWorld(), p, dirO)) instanceof PartPipe) {
			p.move(dirO);
			dist++;

			if (!pipe.connects(dirO)) {
				break;
			}
		}

		tile = getWorld().getTileEntity(p);

		if (tile != null && tile.hasCapability(ModCharsetPipes.CAP_SHIFTER, dir)
				&& isMatchingShifter(tile.getCapability(ModCharsetPipes.CAP_SHIFTER, dir), dir, dist)) {
			shifterDistance[i] = dist;
		} else {
			shifterDistance[i] = 0;
		}

		if (oldDistance != shifterDistance[i]) {
			pipe = PipeUtils.getPipe(getWorld(), getPos().offset(dir), null);
			if (pipe != null) {
				pipe.updateShifterSide(dir);
			}
		}
	}

	private void updateNeighborInfo(boolean sendPacket) {
		if (!getWorld().isRemote) {
			byte oc = connectionCache;

			for (EnumFacing dir : EnumFacing.VALUES) {
				updateConnections(dir);
				updateShifterSide(dir);
			}

			if (sendPacket && connectionCache != oc) {
				sendUpdatePacket();
			}
		}
	}

	private boolean isMatchingShifter(IShifter p, EnumFacing dir, int dist) {
		return p.getDirection() == dir && dist <= p.getShiftDistance();
	}

	private IShifter getNearestShifterInternal(EnumFacing dir) {
		TileEntity tile;

		switch (shifterDistance[dir.ordinal()]) {
			case 0:
				return null;
			case 1:
				tile = getNeighbourTile(dir.getOpposite());
				break;
			default:
				tile = getWorld().getTileEntity(getPos().offset(dir.getOpposite(), shifterDistance[dir.ordinal()]));
		}

		IShifter shifter;
		if (tile != null && tile.hasCapability(ModCharsetPipes.CAP_SHIFTER, dir) && isMatchingShifter(
				shifter = tile.getCapability(ModCharsetPipes.CAP_SHIFTER, dir), dir, Integer.MAX_VALUE)) {
			return shifter;
		} else {
			return null;
		}
	}

	protected IShifter getNearestShifter(EnumFacing dir) {
		if (dir == null) {
			return null;
		} else if (shifterDistance[dir.ordinal()] == 0) {
			return null;
		} else {
			IShifter p = getNearestShifterInternal(dir);
			if (p == null) {
				updateShifterSide(dir);
				return getNearestShifterInternal(dir);
			} else {
				return p;
			}
		}
	}

	protected void addItemClientSide(PipeItem item) {
		if (!getWorld().isRemote) {
			return;
		}

		synchronized (itemSet) {
			Iterator<PipeItem> itemIterator = itemSet.iterator();
			while (itemIterator.hasNext()) {
				PipeItem p = itemIterator.next();
				if (p.id == item.id) {
					itemIterator.remove();
					break;
				}
			}

			if (renderFast && ModCharsetPipes.proxy.stopsRenderFast(getWorld(), item.stack)) {
				renderFast = false;
			}
			itemSet.add(item);
		}
	}

	protected void removeItemClientSide(PipeItem item) {
		if (getWorld().isRemote) {
			synchronized (itemSet) {
				itemSet.remove(item);
			}
		}
	}

	protected boolean injectItemInternal(PipeItem item, EnumFacing dir, boolean simulate) {
		if (item.isValid()) {
			int stuckItems = 0;

			synchronized (itemSet) {
				for (PipeItem p : itemSet) {
					if (p.isStuck()) {
						stuckItems++;

						if (stuckItems >= 1) {
							return false;
						}
					}
				}

				if (!simulate) {
					itemSet.add(item);

					if (renderFast && ModCharsetPipes.proxy.stopsRenderFast(getWorld(), item.stack)) {
						renderFast = false;
					}
				}
			}

			if (!simulate) {
				item.reset(this, dir);
				item.sendPacket(true);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canInjectItems(EnumFacing side) {
		return connects(side);
	}

	@Override
	public int injectItem(ItemStack stack, EnumFacing direction, boolean simulate) {
		if (getWorld().isRemote || !connects(direction)) {
			return 0;
		}

		PipeItem item = new PipeItem(this, stack, direction);

		if (injectItemInternal(item, direction, simulate)) {
			return stack.stackSize;
		} else {
			return 0;
		}
	}

	protected void scheduleRenderUpdate() {
		getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
	}

	@Override
	public void onNeighborBlockChange(Block block) {
		neighborBlockChanged = true;
	}

	public Collection<PipeItem> getPipeItems() {
		return itemSet;
	}

	PipeItem getItemByID(int id) {
		synchronized (itemSet) {
			for (PipeItem p : itemSet) {
				if (p.id == id) {
					return p;
				}
			}
		}

		return null;
	}

	protected void onSyncRequest() {
		// TODO: HACK! HACK! HACK! HACK! HACK! HACK! HACK! HACK!
		synchronized (itemSet) {
			for (PipeItem p : itemSet) {
				p.sendPacket(true);
			}
		}

		fluid.sendPacket(true);
	}

	@Override
	public ItemStack getTravellingStack(EnumFacing side) {
		float targetError = 1000f;
		PipeItem targetItem = null;

		synchronized (itemSet) {
			for (PipeItem p : itemSet) {
				float error;

				if (side == null) {
					error = Math.abs(p.getProgress() - 0.5f);

					if (error > 0.25f) {
						continue;
					}
				} else {
					if (p.getDirection() == null) {
						continue;
					}

					if (p.getProgress() <= 0.25f && side == p.getDirection().getOpposite()) {
						error = Math.abs(p.getProgress() - 0.125f);
					} else if (p.getProgress() >= 0.75f && side == p.getDirection()) {
						error = Math.abs(p.getProgress() - 0.875f);
					} else {
						continue;
					}

					if (error > 0.125f) {
						continue;
					}
				}

				if (error < targetError) {
					targetError = error;
					targetItem = p;
				}
			}
		}

		return targetItem != null ? targetItem.getStack() : null;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state) {
		return ((IExtendedBlockState) state).withProperty(PROPERTY, this);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new ExtendedBlockState(ForgeMultipartModContainer.multipart, new IProperty[0], new IUnlistedProperty[]{PROPERTY});
	}

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return EnumSet.of(PartSlot.CENTER);
	}

	@Override
	public boolean hasFastRenderer() {
		return itemSet.size() == 0 || renderFast;
	}
}
