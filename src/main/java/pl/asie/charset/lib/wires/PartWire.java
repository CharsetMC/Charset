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

package pl.asie.charset.lib.wires;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mcmultipart.MCMultiPartMod;
import mcmultipart.capabilities.ISlottedCapabilityProvider;
import mcmultipart.client.multipart.AdvancedParticleManager;
import mcmultipart.client.multipart.ICustomHighlightPart;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.INormallyOccludingPart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.MultipartRegistry;
import mcmultipart.multipart.OcclusionHelper;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.ProxyClient;
import pl.asie.charset.lib.render.IRenderComparable;
import pl.asie.charset.lib.utils.GenericExtendedProperty;
import pl.asie.charset.lib.utils.RotationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class PartWire extends Multipart implements
		IRenderComparable<PartWire>, ICustomHighlightPart, ISlottedPart,
		INormallyOccludingPart, ITickable, ISlottedCapabilityProvider {
	public static final GenericExtendedProperty<PartWire> PROPERTY = new GenericExtendedProperty<PartWire>("part", PartWire.class);
	private static final Map<WireFactory, AxisAlignedBB[]> BOXES = new HashMap<WireFactory, AxisAlignedBB[]>();

	public WireFace location;
	protected byte internalConnections, externalConnections, cornerConnections, occludedSides, cornerOccludedSides;
	private boolean suLogic, suRender, suConnection;
	private int suNeighbor;
	private WireFactory type;

	public PartWire() {
		scheduleConnectionUpdate();
	}

	@Override
	public ResourceLocation getType() {
		return getFactory().getRegistryName();
	}

	protected WireFactory getFactory() {
		return type;
	}

	public PartWire setFactory(WireFactory factory) {
		this.type = factory;
		return this;
	}

	public boolean calculateConnectionWire(PartWire wire) {
		return wire.getFactory() == getFactory();
	}

	public boolean calculateConnectionNonWire(BlockPos pos, EnumFacing direction) {
		return false;
	}

	public String getDisplayName() {
		return "wire.null";
	}

	@Override
	public float getHardness(PartMOP hit) {
		return 0.2F;
	}

	@Override
	public ResourceLocation getModelPath() {
		return new ResourceLocation("charsetlib:wire");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(AdvancedParticleManager AdvancedParticleManager) {
		// AdvancedParticleManager.addBlockDestroyEffects(getPos(), ProxyClient.rendererWire.getSheet(getFactory()).particle);
		return true;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		addSelectionBoxes(list);
		return list.get(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(PartMOP partMOP, AdvancedParticleManager AdvancedParticleManager) {
		return true;
	}

	@Override
	public void onPartChanged(IMultipart part) {
		scheduleConnectionUpdate();
		scheduleLogicUpdate();
	}

	@Override
	public void onNeighborBlockChange(Block block) {
		if (location != WireFace.CENTER) {
			if (!getFactory().canPlace(getWorld(), getPos(), location)) {
				harvest(null, null);
				return;
			}
		}

		scheduleConnectionUpdate();
		scheduleLogicUpdate();
	}

	@Override
	public IBlockState getExtendedState(IBlockState state) {
		return ((IExtendedBlockState) state).withProperty(PROPERTY, this);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new ExtendedBlockState(MCMultiPartMod.multipart, new IProperty[0], new IUnlistedProperty[]{PROPERTY});
	}

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return EnumSet.of(WireUtils.getSlotForFace(location));
	}

	protected ItemStack getItemStack() {
		return new ItemStack(WireManager.ITEM, 1, WireManager.REGISTRY.getId(getFactory()) << 1 | (location == WireFace.CENTER ? 1 : 0));
	}

	@Override
	public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {
		return getItemStack();
	}

	@Override
	public List<ItemStack> getDrops() {
		return Arrays.asList(getItemStack());
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		buf.writeByte(WireManager.REGISTRY.getId(getFactory()));
		buf.writeByte(location.ordinal());
		buf.writeByte(internalConnections);
		buf.writeByte(externalConnections);
		if (location != WireFace.CENTER) {
			buf.writeByte(cornerConnections);
		}
	}

	public void handlePacket(ByteBuf buf) {
		location = WireFace.VALUES[buf.readByte()];

		int oldIC = internalConnections;
		int oldEC = externalConnections;
		int oldCC = cornerConnections;

		internalConnections = buf.readByte();
		externalConnections = buf.readByte();
		cornerConnections = location == WireFace.CENTER ? 0 : buf.readByte();

		if (oldIC != internalConnections || oldEC != externalConnections || oldCC != cornerConnections) {
			markRenderUpdate();
		}
	}

	@Override
	public final void readUpdatePacket(PacketBuffer buf) {
		setFactory(WireManager.REGISTRY.getObjectById(buf.readByte()));

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
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("f")) { // Migration stuffs
			setFactory(WireManager.REGISTRY.getObjectById(nbt.getByte("f")));
		}
		location = WireFace.VALUES[nbt.getByte("l")];
		internalConnections = nbt.getByte("iC");
		externalConnections = nbt.getByte("eC");
		cornerConnections = nbt.getByte("cC");
		occludedSides = nbt.getByte("oS");
		cornerOccludedSides = nbt.getByte("coS");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setByte("f", (byte) WireManager.REGISTRY.getId(getFactory()));
		nbt.setByte("l", (byte) location.ordinal());
		nbt.setByte("iC", internalConnections);
		nbt.setByte("eC", externalConnections);
		if (location != WireFace.CENTER) {
			nbt.setByte("cC", cornerConnections);
		}
		nbt.setByte("oS", occludedSides);
		nbt.setByte("coS", cornerOccludedSides);
		return nbt;
	}

	@Override
	public void onAdded() {
		scheduleConnectionUpdate();
		scheduleLogicUpdate();
	}

	@Override
	public void onRemoved() {
		neighborUpdate(0);
	}

	private AxisAlignedBB[] getBoxes() {
		AxisAlignedBB[] boxes = BOXES.get(this.type);

		if (boxes == null) {
			boxes = new AxisAlignedBB[43 + 24];
			float xMin = 0.5f - type.getWidth() / 2;
			float xMax = 0.5f + type.getWidth() / 2;
			float y = type.getHeight();

			for (int j = 0; j < 6; j++) {
				EnumFacing f = EnumFacing.getFront(j);
				EnumFacing[] faces = WireUtils.getConnectionsForRender(WireFace.get(f));
				for (int i = 0; i < faces.length; i++) {
					if (i >= 2) {
						if (faces[i].getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
							boxes[j * 5 + i + 1] = RotationUtils.rotateFace(new AxisAlignedBB(0, 0, xMin, xMin, y, xMax), f);
							boxes[43 + j * 4 + i] = RotationUtils.rotateFace(new AxisAlignedBB(0, 0, xMin, y, y, xMax), f);
						} else {
							boxes[j * 5 + i + 1] = RotationUtils.rotateFace(new AxisAlignedBB(xMax, 0, xMin, 1, y, xMax), f);
							boxes[43 + j * 4 + i] = RotationUtils.rotateFace(new AxisAlignedBB(1 - y, 0, xMin, 1, y, xMax), f);
						}
					} else {
						if (faces[i].getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
							boxes[j * 5 + i + 1] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, 0, xMax, y, xMin), f);
							boxes[43 + j * 4 + i] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, 0, xMax, y, y), f);
						} else {
							boxes[j * 5 + i + 1] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, xMax, xMax, y, 1), f);
							boxes[43 + j * 4 + i] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, 1 - y, xMax, y, 1), f);
						}
					}
				}
				boxes[j * 5 + 0] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, xMin, xMax, y, xMax), f);
				boxes[31 + j] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, y, xMin, xMax, xMin, xMax), f);
				boxes[37 + j] = RotationUtils.rotateFace(new AxisAlignedBB(xMin, 0, xMin, xMax, xMin, xMax), f);
			}
			boxes[30] = new AxisAlignedBB(xMin, xMin, xMin, xMax, xMax, xMax);
			BOXES.put(this.type, boxes);
		}

		return boxes;
	}

	public AxisAlignedBB getCenterBox(int i) {
		AxisAlignedBB[] boxes = getBoxes();

		return boxes[6 * 5 + i];
	}

	public AxisAlignedBB getSelectionBox(int i) {
		return getBox((i > 0 && location == WireFace.CENTER) ? (i + 6) : i);
	}

	public AxisAlignedBB getBox(int i) {
		AxisAlignedBB[] boxes = getBoxes();

		return boxes[location.ordinal() * 5 + i];
	}

	public AxisAlignedBB getCornerCollisionBox(EnumFacing facing) {
		EnumFacing[] facings = WireUtils.getConnectionsForRender(location);
		for (int i = 0; i < facings.length; i++) {
			if (facing == facings[i]) {
				return getCornerBox(i);
			}
		}

		return null; // !?
	}

	private AxisAlignedBB getCornerBox(int i) {
		AxisAlignedBB[] boxes = getBoxes();

		return boxes[43 + location.ordinal() * 4 + i];
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		list.add(getBox(0));
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(getSelectionBox(0));

		EnumFacing[] faces = WireUtils.getConnectionsForRender(location);
		for (int i = 0; i < faces.length; i++) {
			if (connectsAny(faces[i])) {
				list.add(getSelectionBox(i + 1));
			}
		}
	}

	protected abstract void logicUpdate();

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		AxisAlignedBB bb = getBox(0);
		if (mask.intersectsWith(bb)) {
			list.add(bb);
		}

		EnumFacing[] faces = WireUtils.getConnectionsForRender(location);
		for (int i = 0; i < faces.length; i++) {
			if (connectsAny(faces[i])) {
				bb = getBox(i + 1);
				if (mask.intersectsWith(bb)) {
					list.add(bb);
				}
			}
		}
	}

	public void scheduleRenderUpdate() {
		suRender = true;
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	protected void neighborUpdate(int sides) {
		if (getContainer() != null) {
			for (IMultipart multipart : getContainer().getParts()) {
				if (multipart instanceof PartWire) {
					multipart.onNeighborBlockChange(MCMultiPartMod.multipart);
				}
			}
		}

		World world = this.getWorld();
		BlockPos pos = this.getPos();
		if (world != null) {
			world.notifyNeighborsRespectDebug(pos, MCMultiPartMod.multipart);
			for (EnumFacing facing : EnumFacing.VALUES) {
				world.notifyNeighborsOfStateExcept(pos.offset(facing), MCMultiPartMod.multipart, facing.getOpposite());
			}
		}
	}

	@Override
	public void update() {
		World world = getWorld();
		if (world == null) {
			return;
		}

		if (location == null) {
			getContainer().removePart(this);
			return;
		}

		if (suConnection) {
			suConnection = false;
			updateConnections();
		}

		if (suNeighbor != 0) {
			int it = suNeighbor;
			suNeighbor = 0;
			neighborUpdate(it);
		}

		if (suLogic) {
			suLogic = false;
			logicUpdate();
		}

		if (suNeighbor != 0) {
			int it = suNeighbor;
			suNeighbor = 0;
			neighborUpdate(it);
		}

		if (suRender) {
			suRender = false;
			if (world.isRemote) {
				markRenderUpdate();
			} else {
				sendUpdatePacket();
			}
		}
	}

	public boolean isOccluded(EnumFacing face) {
		if (suConnection) {
			suConnection = false;
			updateConnections();
		}
		return (occludedSides & (1 << face.ordinal())) != 0;
	}

	public boolean isCornerOccluded(EnumFacing face) {
		if (suConnection) {
			suConnection = false;
			updateConnections();
		}
		return isOccluded(face) || (cornerOccludedSides & (1 << face.ordinal())) != 0;
	}

	public void updateConnections() {
		Set<WireFace> validSides = EnumSet.noneOf(WireFace.class);
		Set<WireFace> invalidCornerSides = EnumSet.noneOf(WireFace.class);

		for (WireFace facing : WireFace.VALUES) {
			if (facing == location) {
				continue;
			}

			if (facing != WireFace.CENTER && location != WireFace.CENTER && location.facing.getAxis() == facing.facing.getAxis()) {
				continue;
			}

			validSides.add(facing);
		}

		int oldConnectionCache = internalConnections << 12 | externalConnections << 6 | cornerConnections;
		internalConnections = externalConnections = cornerConnections = occludedSides = cornerOccludedSides = 0;

		// Occlusion test

		EnumFacing[] connFaces = WireUtils.getConnectionsForRender(location);
		List<IMultipart> parts = new ArrayList<IMultipart>();
		for (IMultipart p : getContainer().getParts()) {
			if (p != this && p instanceof INormallyOccludingPart && !(p instanceof PartWire)) {
				parts.add(p);
			}
		}

		if (parts.size() > 0) {
			for (int i = 0; i < connFaces.length; i++) {
				WireFace face = WireFace.get(connFaces[i]);
				if (validSides.contains(face)) {
					boolean found = false;
					AxisAlignedBB mask = getBox(i + 1);
					if (mask != null) {
						if (!OcclusionHelper.occlusionTest(OcclusionHelper.boxes(mask), p -> p == this, parts)) {
							occludedSides |= 1 << connFaces[i].ordinal();
							validSides.remove(face);
							found = true;
						}
					}

					if (!found && location != WireFace.CENTER) {
						BlockPos cPos = getPos().offset(connFaces[i]);
						AxisAlignedBB cornerMask = getCornerBox(i ^ 1);
						if (cornerMask != null) {
							IMultipartContainer cornerContainer = MultipartHelper.getPartContainer(getWorld(), cPos);
							if (cornerContainer != null) {
								if (!OcclusionHelper.occlusionTest(OcclusionHelper.boxes(cornerMask), cornerContainer.getParts())) {
									cornerOccludedSides |= 1 << connFaces[i].ordinal();
									invalidCornerSides.add(face);
								}
							} else {
								List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
								IBlockState cState = getWorld().getBlockState(cPos);
								cState.addCollisionBoxToList(getWorld(), cPos,
										cornerMask.offset(cPos.getX(), cPos.getY(), cPos.getZ()), boxes, null);
								if (boxes.size() > 0) {
									cornerOccludedSides |= 1 << connFaces[i].ordinal();
									invalidCornerSides.add(face);
								}
							}
						}
					}
				}
			}
		}

		if (validSides.contains(WireFace.CENTER)) {
			AxisAlignedBB mask = getCenterBox(1 + location.ordinal());
			if (mask != null) {
				if (!OcclusionHelper.occlusionTest(OcclusionHelper.boxes(mask), p -> p == this, parts)) {
					occludedSides |= 1 << 6;
					validSides.remove(WireFace.CENTER);
				}
			}
		}

		// Connection test

		for (WireFace facing : validSides) {
			if (WireUtils.canConnectInternal(this, facing)) {
				internalConnections |= 1 << facing.ordinal();
			} else if (facing != WireFace.CENTER) {
				if (WireUtils.canConnectExternal(this, facing.facing)) {
					externalConnections |= 1 << facing.ordinal();
				} else if (location != WireFace.CENTER && !invalidCornerSides.contains(facing) && WireUtils.canConnectCorner(this, facing.facing)) {
					cornerConnections |= 1 << facing.ordinal();
				}
			}
		}

		int newConnectionCache = internalConnections << 12 | externalConnections << 6 | cornerConnections;

		if (oldConnectionCache != newConnectionCache) {
			scheduleNeighborUpdate((oldConnectionCache ^ newConnectionCache) >> 6);
			scheduleLogicUpdate();
			scheduleRenderUpdate();
		}
	}

	protected void scheduleNeighborUpdate(int et) {
		suNeighbor = (et & 0x3f) | 0x10000;
	}

	protected void scheduleNeighborUpdate() {
		scheduleNeighborUpdate(0);
	}

	protected void scheduleLogicUpdate() {
		suLogic = true;
	}

	protected void scheduleConnectionUpdate() {
		suConnection = true;
	}

	public boolean connectsInternal(WireFace side) {
		return (internalConnections & (1 << side.ordinal())) != 0;
	}

	public boolean connectsExternal(EnumFacing side) {
		return (externalConnections & (1 << side.ordinal())) != 0;
	}

	public boolean connectsAny(EnumFacing direction) {
		return ((internalConnections | externalConnections | cornerConnections) & (1 << direction.ordinal())) != 0;
	}

	public boolean connectsCorner(EnumFacing direction) {
		return (cornerConnections & (1 << direction.ordinal())) != 0;
	}

	public boolean connects(EnumFacing direction) {
		return ((internalConnections | externalConnections) & (1 << direction.ordinal())) != 0;
	}

	@SideOnly(Side.CLIENT)
	public int getRenderColor() {
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean drawHighlight(PartMOP partMOP, EntityPlayer player, float v) {
		ModCharsetLib.proxy.drawWireHighlight(this);
		return true;
	}

	public void setConnectionsForItemRender() {
		internalConnections = 0x3F;
		externalConnections = 0;
		cornerConnections = 0;
	}

	@Override
	public boolean renderEquals(PartWire other) {
		return other.type == type
				&& other.location == location
				&& other.internalConnections == internalConnections
				&& other.externalConnections == externalConnections
				&& other.cornerConnections == cornerConnections;
	}

	@Override
	public int renderHashCode() {
		return Objects.hash(type, location, internalConnections, externalConnections, cornerConnections);
	}
}
