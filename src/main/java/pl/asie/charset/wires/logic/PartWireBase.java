package pl.asie.charset.wires.logic;

import java.util.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mcmultipart.client.multipart.AdvancedEffectRenderer;
import mcmultipart.multipart.*;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.MCMultiPartMod;
import mcmultipart.capabilities.ISlottedCapabilityProvider;
import mcmultipart.client.multipart.ICustomHighlightPart;
import mcmultipart.raytrace.PartMOP;
import pl.asie.charset.api.wires.IWire;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.render.IRenderComparable;
import pl.asie.charset.lib.utils.RotationUtils;
import pl.asie.charset.wires.ModCharsetWires;
import pl.asie.charset.wires.ProxyClient;
import pl.asie.charset.wires.WireKind;
import pl.asie.charset.wires.WireUtils;

public abstract class PartWireBase extends Multipart implements
		ICustomHighlightPart, IRedstonePart.ISlottedRedstonePart, IRenderComparable<PartWireBase>,
		INormallyOccludingPart, ITickable, ISlottedCapabilityProvider, IWire {
	protected static final boolean DEBUG = false;
	private static final Map<WireKind, AxisAlignedBB[]> BOXES = new HashMap<WireKind, AxisAlignedBB[]>();

	public static final Property PROPERTY = new Property();

	private static class Property implements IUnlistedProperty<PartWireBase> {
		private Property() {

		}

		@Override
		public String getName() {
			return "wireTile";
		}

		@Override
		public boolean isValid(PartWireBase value) {
			return true;
		}

		@Override
		public Class<PartWireBase> getType() {
			return PartWireBase.class;
		}

		@Override
		public String valueToString(PartWireBase value) {
			return "!?";
		}
	}

	public WireKind type;
	public WireFace location;
	protected byte internalConnections, externalConnections, cornerConnections, occludedSides, cornerOccludedSides;
	private boolean suPropagation, suRender, suConnection;
	private int suNeighbor;

	private final EnumSet<EnumFacing> propagationDirs = EnumSet.noneOf(EnumFacing.class);

	public PartWireBase() {
		scheduleConnectionUpdate();
	}

	public abstract void propagate(int color);

	public abstract int getSignalLevel();

	public abstract int getRedstoneLevel();

	@Override
	public float getHardness(PartMOP hit) {
		return 0.2F;
	}

	@Override
	public ResourceLocation getModelPath() {
		return getType();
	}

	@Override
	public ResourceLocation getType() {
		return new ResourceLocation("charsetwires:wire");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(AdvancedEffectRenderer advancedEffectRenderer) {
		// TODO
		//advancedEffectRenderer.addBlockDestroyEffects(getPos(), ProxyClient.rendererWire.handlePartState(getExtendedState(MultipartRegistry.getDefaultState(this).getBaseState())).getParticleTexture());
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
	public boolean addHitEffects(PartMOP partMOP, AdvancedEffectRenderer advancedEffectRenderer) {
		return true;
	}

	@Override
	public void onPartChanged(IMultipart part) {
		scheduleConnectionUpdate();
		schedulePropagationUpdate();
	}

	@Override
	public void onNeighborBlockChange(Block block) {
		if (location != WireFace.CENTER) {
			if (!WireUtils.canPlaceWire(getWorld(), getPos().offset(location.facing), location.facing.getOpposite())) {
				harvest(null, null);
				return;
			}
		}

		scheduleConnectionUpdate();
		schedulePropagationUpdate();
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

	private ItemStack getItemStack() {
		return new ItemStack(ModCharsetWires.wire, 1, type.ordinal() << 1 | (location == WireFace.CENTER ? 1 : 0));
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
		buf.writeByte(type.ordinal());
		buf.writeByte(location.ordinal());
		buf.writeByte(internalConnections);
		buf.writeByte(externalConnections);
		if (location != WireFace.CENTER) {
			buf.writeByte(cornerConnections);
		}
	}

	public void handlePacket(ByteBuf buf) {
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
		type = WireKind.VALUES[buf.readByte()];
		location = WireFace.VALUES[buf.readByte()];

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
		type = WireKind.VALUES[nbt.getByte("t")];
		location = WireFace.VALUES[nbt.getByte("l")];
		internalConnections = nbt.getByte("iC");
		externalConnections = nbt.getByte("eC");
		cornerConnections = nbt.getByte("cC");
		occludedSides = nbt.getByte("oS");
		cornerOccludedSides = nbt.getByte("coS");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setByte("t", (byte) type.ordinal());
		nbt.setByte("l", (byte) location.ordinal());
		nbt.setByte("iC", internalConnections);
		nbt.setByte("eC", externalConnections);
		if (location != WireFace.CENTER) {
			nbt.setByte("cC", cornerConnections);
		}
		nbt.setByte("oS", occludedSides);
		nbt.setByte("coS", cornerOccludedSides);
	}

	@Override
	public void onAdded() {
		scheduleConnectionUpdate();
		schedulePropagationUpdate();
	}

	@Override
	public void onRemoved() {
		pokeExtendedNeighbors();
	}

	private AxisAlignedBB[] getBoxes() {
		AxisAlignedBB[] boxes = BOXES.get(this.type);

		if (boxes == null) {
			boxes = new AxisAlignedBB[43 + 24];
			float xMin = 0.5f - WireUtils.getWireHitboxWidth(this) / 2;
			float xMax = 0.5f + WireUtils.getWireHitboxWidth(this) / 2;
			float y = WireUtils.getWireHitboxHeight(this);

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

	private void pokeExtendedNeighbors() {
		if (getContainer() != null) {
			for (IMultipart multipart : getContainer().getParts()) {
				if (multipart instanceof PartWireBase) {
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
			propagateExternalTiles(it);
			pokeExtendedNeighbors();
		}

		if (suPropagation) {
			suPropagation = false;
			if (!world.isRemote) {
				onSignalChanged(-1);
			}
		}

		if (suNeighbor != 0) {
			int it = suNeighbor;
			suNeighbor = 0;
			propagateExternalTiles(it);
			pokeExtendedNeighbors();
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
			if (p != this && p instanceof INormallyOccludingPart && !(p instanceof PartWireBase)) {
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
						if (!OcclusionHelper.occlusionTest(parts, p -> p == this, mask)) {
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
								if (!OcclusionHelper.occlusionTest(cornerContainer.getParts(), cornerMask)) {
									cornerOccludedSides |= 1 << connFaces[i].ordinal();
									invalidCornerSides.add(face);
								}
							} else {
								List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
								IBlockState cState = getWorld().getBlockState(cPos);
								cState.getBlock().addCollisionBoxToList(cState, getWorld(), cPos,
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
				if (!OcclusionHelper.occlusionTest(parts, p -> p == this, mask)) {
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
			schedulePropagationUpdate();
			scheduleRenderUpdate();
		}
	}

	protected void propagateExternalTiles(int i) {
		for (int j = 0; j < 6; j++) {
			if ((i & (1 << j)) != 0) {
				EnumFacing facing = EnumFacing.getFront(j);
				TileEntity tile = getWorld().getTileEntity(getPos().offset(facing));
				if (tile != null) {
					if (type.type() == WireType.BUNDLED) {
						if (tile.hasCapability(Capabilities.BUNDLED_RECEIVER, facing.getOpposite())) {
							tile.getCapability(Capabilities.BUNDLED_RECEIVER, facing.getOpposite()).onBundledInputChange();
						}
					} else {
						if (tile.hasCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite())) {
							tile.getCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite()).onRedstoneInputChange();
						}
					}
				}
			}
		}
	}

	protected void scheduleNeighborUpdate(int et) {
		suNeighbor = (et & 0x3f) | 0x10000;
	}

	protected void scheduleNeighborUpdate() {
		scheduleNeighborUpdate(0);
	}

	protected void schedulePropagationUpdate() {
		suPropagation = true;
	}

	protected void scheduleConnectionUpdate() {
		suConnection = true;
	}

	protected abstract void onSignalChanged(int color);

	protected void propagateNotifyCorner(EnumFacing side, EnumFacing direction, int color) {
		PartWireBase wire = WireUtils.getWire(MultipartHelper.getPartContainer(getWorld(), getPos().offset(side).offset(direction)), WireFace.get(direction.getOpposite()));
		if (wire != null) {
			wire.onSignalChanged(color);
		}
	}

	protected void propagateNotify(EnumFacing facing, int color) {
		PartWireBase wire = WireUtils.getWire(MultipartHelper.getPartContainer(getWorld(), getPos().offset(facing)), location);
		if (wire != null) {
			wire.onSignalChanged(color);
		} else {
			propagationDirs.add(facing);
		}
	}

	protected void finishPropagation() {
		for (EnumFacing facing : propagationDirs) {
			TileEntity nt = getWorld().getTileEntity(getPos().offset(facing));
			boolean found = false;
			if (nt != null) {
				if (type.type() == WireType.BUNDLED) {
					if (nt.hasCapability(Capabilities.BUNDLED_RECEIVER, facing.getOpposite())) {
						nt.getCapability(Capabilities.BUNDLED_RECEIVER, facing.getOpposite()).onBundledInputChange();
						found = true;
					}
				} else {
					if (nt.hasCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite())) {
						nt.getCapability(Capabilities.REDSTONE_RECEIVER, facing.getOpposite()).onRedstoneInputChange();
						found = true;
					}
				}
			}

			if (type.type() != WireType.BUNDLED && !found) {
				getWorld().notifyBlockOfStateChange(getPos().offset(facing), MCMultiPartMod.multipart);
			}
		}

		propagationDirs.clear();
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

	public int getBundledSignalLevel(int i) {
		return 0;
	}

	@Override
	public boolean canConnectRedstone(EnumFacing facing) {
		return WireUtils.WIRES_CONNECT && type.type() != WireType.BUNDLED && connectsExternal(facing);
	}

	@Override
	public int getWeakSignal(EnumFacing facing) {
		if (connectsWeak(facing)) {
			return getRedstoneLevel();
		} else {
			return 0;
		}
	}

	public boolean connectsWeak(EnumFacing facing) {
		if (type.type() == WireType.BUNDLED) {
			return false;
		}

		// Block any signals if there's a wire on the target face
		if (location.facing == facing) {
			return true;
		} else {
			if (connects(facing) || type.type() == WireType.NORMAL) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean drawHighlight(PartMOP partMOP, EntityPlayer player, float v) {
		ModCharsetWires.proxy.drawWireHighlight(this);
		return true;
	}

	@Override
	public WireType getWireType() {
		return type.type();
	}

	@Override
	public int getStrongSignal(EnumFacing facing) {
		if (type.type() == WireType.NORMAL && location.facing == facing) {
			return getRedstoneLevel();
		} else {
			return 0;
		}
	}

	public void setConnectionsForItemRender() {
		internalConnections = 0x3F;
		externalConnections = 0;
		cornerConnections = 0;
	}

	@Override
	public boolean renderEquals(PartWireBase other) {
		return other.type == type
				&& other.location == location
				&& other.internalConnections == internalConnections
				&& other.externalConnections == externalConnections
				&& other.cornerConnections == cornerConnections
				&& (getWireType() == WireType.INSULATED || other.getRedstoneLevel() == getRedstoneLevel());
	}

	@Override
	public int renderHashCode() {
		return Objects.hash(type, location, internalConnections, externalConnections, cornerConnections, getWireType() == WireType.INSULATED ? 0 : getRedstoneLevel());
	}
}
