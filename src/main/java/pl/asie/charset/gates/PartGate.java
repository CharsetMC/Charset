package pl.asie.charset.gates;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fmp.ForgeMultipart;
import net.minecraftforge.fmp.multipart.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import net.minecraftforge.fmp.capabilities.ISlottedCapabilityProvider;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.render.IRenderComparable;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RedstoneUtils;
import pl.asie.charset.lib.utils.RotationUtils;

public abstract class PartGate extends Multipart implements IRenderComparable<PartGate>,
		IRedstonePart.ISlottedRedstonePart, INormallyOccludingPart, ISlottedCapabilityProvider, ITickable {
	private class RedstoneCommunications implements IBundledEmitter, IBundledReceiver, IRedstoneEmitter, IRedstoneReceiver {
		private final EnumFacing side;

		RedstoneCommunications(EnumFacing side) {
			this.side = side;
		}

		@Override
		public byte[] getBundledSignal() {
			return new byte[0];
		}

		@Override
		public void onBundledInputChange() {
			onChanged();
		}

		@Override
		public int getRedstoneSignal() {
			Connection type = getType(side);
			return type.isOutput() && type.isRedstone() ? getValueOutside(side) : 0;
		}

		@Override
		public void onRedstoneInputChange() {
			onChanged();
		}
	}

	private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];
	private static final Vec3d[][] HIT_VECTORS = new Vec3d[6][];
	private final RedstoneCommunications[] COMMS = new RedstoneCommunications[4];

	static {
		for (int i = 0; i < 6; i++) {
			EnumFacing facing = EnumFacing.getFront(i);
			BOXES[i] = RotationUtils.rotateFace(new AxisAlignedBB(0, 0, 0, 1, 0.125, 1), facing);

			HIT_VECTORS[i] = new Vec3d[4];
			if (facing.getAxis() != EnumFacing.Axis.Y) {
				HIT_VECTORS[i][1] = RotationUtils.rotateVec(new Vec3d(0.5f, 0.125f, 0.0f), facing);
				HIT_VECTORS[i][0] = RotationUtils.rotateVec(new Vec3d(0.5f, 0.125f, 1.0f), facing);
			} else {
				HIT_VECTORS[i][0] = RotationUtils.rotateVec(new Vec3d(0.5f, 0.125f, 0.0f), facing);
				HIT_VECTORS[i][1] = RotationUtils.rotateVec(new Vec3d(0.5f, 0.125f, 1.0f), facing);
			}

			if (facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) {
				HIT_VECTORS[i][3] = RotationUtils.rotateVec(new Vec3d(0.0f, 0.125f, 0.5f), facing);
				HIT_VECTORS[i][2] = RotationUtils.rotateVec(new Vec3d(1.0f, 0.125f, 0.5f), facing);
			} else {
				HIT_VECTORS[i][2] = RotationUtils.rotateVec(new Vec3d(0.0f, 0.125f, 0.5f), facing);
				HIT_VECTORS[i][3] = RotationUtils.rotateVec(new Vec3d(1.0f, 0.125f, 0.5f), facing);
			}
		}
	}

	private static class Property implements IUnlistedProperty<PartGate> {
		private Property() {

		}

		@Override
		public String getName() {
			return "gate";
		}

		@Override
		public boolean isValid(PartGate value) {
			return true;
		}

		@Override
		public Class<PartGate> getType() {
			return PartGate.class;
		}

		@Override
		public String valueToString(PartGate value) {
			return "!?";
		}
	}

	public enum Connection {
		NONE,
		INPUT,
		OUTPUT,
		INPUT_OUTPUT,
		INPUT_ANALOG,
		OUTPUT_ANALOG,
		INPUT_BUNDLED,
		OUTPUT_BUNDLED;

		public boolean isInput() {
			return this == INPUT || this == INPUT_ANALOG || this == INPUT_OUTPUT || this == INPUT_BUNDLED;
		}

		public boolean isOutput() {
			return this == OUTPUT || this == OUTPUT_ANALOG || this == INPUT_OUTPUT || this == OUTPUT_BUNDLED;
		}

		public boolean isRedstone() {
			return this == INPUT || this == OUTPUT || this == INPUT_ANALOG || this == OUTPUT_ANALOG || this == INPUT_OUTPUT;
		}

		public boolean isDigital() {
			return this == INPUT || this == OUTPUT || this == INPUT_OUTPUT;
		}

		public boolean isAnalog() {
			return this == INPUT_ANALOG || this == OUTPUT_ANALOG;
		}

		public boolean isBundled() {
			return this == INPUT_BUNDLED || this == OUTPUT_BUNDLED;
		}
	}

	public enum State {
		NO_RENDER,
		OFF,
		ON,
		DISABLED;

		public State invert() {
			switch (this) {
				case OFF:
					return ON;
				case ON:
					return OFF;
				default:
					return this;
			}
		}

		public static State input(byte i) {
			return i > 0 ? ON : OFF;
		}

		public static State bool(boolean v) {
			return v ? ON : OFF;
		}
	}

	protected byte enabledSides, invertedSides;
	protected boolean mirrored;

	private int pendingTick;
	private byte[] values = new byte[4];

	private EnumFacing side = EnumFacing.DOWN;
	private EnumFacing top = EnumFacing.NORTH;

	public PartGate() {
		enabledSides = getSideMask();

		COMMS[0] = new RedstoneCommunications(EnumFacing.NORTH);
		COMMS[1] = new RedstoneCommunications(EnumFacing.SOUTH);
		COMMS[2] = new RedstoneCommunications(EnumFacing.WEST);
		COMMS[3] = new RedstoneCommunications(EnumFacing.EAST);
	}

	PartGate setSide(EnumFacing facing) {
		this.side = facing;
		return this;
	}

	PartGate setTop(EnumFacing facing) {
		this.top = facing;
		return this;
	}

	PartGate setInvertedSides(int sides) {
		this.invertedSides = (byte) sides;
		return this;
	}

	public boolean isMirrored() {
		return mirrored;
	}

	public Connection getType(EnumFacing dir) {
		return dir == EnumFacing.NORTH ? Connection.OUTPUT : Connection.INPUT;
	}

	public abstract State getLayerState(int id);

	public abstract State getTorchState(int id);

	public boolean canBlockSide(EnumFacing side) {
		return getType(side).isInput();
	}

	public boolean canInvertSide(EnumFacing side) {
		return getType(side).isDigital();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, PartSlot partSlot, EnumFacing direction) {
		if (partSlot.f1 == side && direction.getAxis() != side.getAxis()) {
			EnumFacing dir = realToGate(direction);
			if (isSideOpen(dir)) {
				Connection conn = getType(dir);
				if (capability == Capabilities.BUNDLED_EMITTER && conn.isOutput() && conn.isBundled()) {
					return true;
				} else if (capability == Capabilities.BUNDLED_RECEIVER && conn.isInput() && conn.isBundled()) {
					return true;
				} else if (capability == Capabilities.REDSTONE_EMITTER && conn.isOutput() && conn.isRedstone()) {
					return true;
				} else if (capability == Capabilities.REDSTONE_RECEIVER && conn.isInput() && conn.isRedstone()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, PartSlot partSlot, EnumFacing enumFacing) {
		if (!hasCapability(capability, partSlot, enumFacing)) {
			return null;
		}
		EnumFacing dir = realToGate(enumFacing);
		if (dir.ordinal() >= 2) {
			return (T) COMMS[dir.ordinal() - 2];
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getPickPart(EntityPlayer player, RayTraceResult hit) {
		return ItemGate.getStack(this, true);
	}

	@Override
	public List<ItemStack> getDrops() {
		return Arrays.asList(ItemGate.getStack(this, false));
	}

	public List<ItemStack> getDrops(EntityPlayer player) {
		return Arrays.asList(ItemGate.getStack(this, player != null ? (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand()) > 0) : false));
	}

	// Refer to Multipart.class when updating
	@Override
	public void harvest(EntityPlayer player, RayTraceResult hit) {
		World world = getWorld();
		BlockPos pos = getPos();
		double x = pos.getX() + 0.5, y = pos.getY() + 0.25, z = pos.getZ() + 0.5;

		if ((player == null || !player.capabilities.isCreativeMode) && !world.isRemote && world.getGameRules().getBoolean("doTileDrops")
				&& !world.restoringBlockSnapshots) {
			for (ItemStack stack : getDrops(player)) {
				EntityItem item = new EntityItem(world, x, y, z, stack);
				item.setDefaultPickupDelay();
				world.spawnEntityInWorld(item);
			}
		}
		getContainer().removePart(this);
	}

	@Override
	public void update() {
		if (getWorld() != null && !getWorld().isRemote && pendingTick > 0) {
			pendingTick--;
			if (pendingTick == 0) {
				if (tick()) {
					notifyBlockUpdate();
					sendUpdatePacket();
				}
			}
		}
	}

	protected boolean tick() {
		return updateInputs();
	}

	private boolean updateInputs() {
		byte[] oldValues = new byte[4];
		boolean changed = false;

		System.arraycopy(values, 0, oldValues, 0, 4);

		for (int i = 0; i <= 3; i++) {
			EnumFacing facing = EnumFacing.getFront(i + 2);
			Connection conn = getType(facing);
			if (conn.isInput() && conn.isRedstone()) {
				values[i] = 0;

				if (isSideOpen(facing)) {
					EnumFacing real = gateToReal(facing);
					World w = getWorld();
					BlockPos p = getPos().offset(real);
					IMultipartContainer container = MultipartHelper.getPartContainer(w, p);
					if (container != null) {
						values[i] = (byte) MultipartRedstoneHelper.getWeakSignal(container, real.getOpposite(), side);
					} else {
						TileEntity tile = w.getTileEntity(p);
						if (tile != null && tile.hasCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite())) {
							values[i] = (byte) tile.getCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite()).getRedstoneSignal();
						} else {
							IBlockState s = w.getBlockState(p);
							if (RedstoneUtils.canConnectFace(w, p, s, real, side)) {
								if (s.getBlock() instanceof BlockRedstoneWire) {
									values[i] = s.getValue(BlockRedstoneWire.POWER).byteValue();
								} else {
									values[i] = (byte) s.getWeakPower(w, p, real);
								}
							}
						}
					}

					if (conn.isDigital()) {
						values[i] = values[i] != 0 ? (byte) 15 : 0;
					}

					if (isSideInverted(facing)) {
						values[i] = values[i] != 0 ? 0 : (byte) 15;
					}
				}
			}
		}

		for (int i = 0; i <= 3; i++) {
			EnumFacing facing = EnumFacing.getFront(i + 2);
			Connection conn = getType(facing);
			if (conn.isOutput() && conn.isRedstone()) {
				values[i] = calculateOutputInside(facing);
			}

			if (values[i] != oldValues[i]) {
				changed = true;
			}
		}

		return changed;
	}

	public byte getValueInside(EnumFacing side) {
		return values[side.ordinal() - 2];
	}

	protected byte getValueOutside(EnumFacing side) {
		if (isSideInverted(side) && isSideOpen(side)) {
			return values[side.ordinal() - 2] != 0 ? 0 : (byte) 15;
		} else {
			return values[side.ordinal() - 2];
		}
	}

	public boolean getInverterState(EnumFacing facing) {
		byte value = getType(facing).isInput() ? getValueOutside(facing) : getValueInside(facing);
		return value == 0;
	}

	protected void onChanged() {
		scheduleTick();
	}

	protected void scheduleTick() {
		if (pendingTick == 0) {
			pendingTick = 2;
		}
	}

	@Override
	public void onAdded() {
		pendingTick = 1;
	}

	@Override
	public void onLoaded() {
		pendingTick = 1;
	}

	@Override
	public void onPartChanged(IMultipart part) {
		onChanged();
	}

	@Override
	public void onNeighborBlockChange(Block block) {
		if (!getWorld().isSideSolid(getPos().offset(side), side.getOpposite())) {
			harvest(null, null);
			return;
		}

		onChanged();
	}

	@Override
	public boolean canRotatePartAround(EnumFacing axis) {
		return axis.getAxis() == side.getAxis();
	}

	@Override
	public void rotatePartAround(EnumFacing axis) {
		if (axis.getAxis() == side.getAxis()) {
			if (axis.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
				top = top.rotateY();
			} else {
				top = top.rotateYCCW();
			}

			notifyBlockUpdate();
			onChanged();
			sendUpdatePacket();
		}
	}

	public EnumFacing getSide() {
		return side;
	}

	public EnumFacing getTop() {
		return top;
	}

	protected byte getSideMask() {
		byte j = 0;
		for (int i = 0; i <= 3; i++) {
			if (getType(EnumFacing.getFront(i + 2)) != Connection.NONE) {
				j |= (1 << i);
			}
		}
		return j;
	}

	protected abstract byte calculateOutputInside(EnumFacing side);

	public boolean isSideOpen(EnumFacing side) {
		return (enabledSides & (1 << (side.ordinal() - 2))) != 0;
	}

	public boolean isSideInverted(EnumFacing side) {
		return (invertedSides & (1 << (side.ordinal() - 2))) != 0;
	}

	private EnumFacing getClosestFace(Vec3d vec) {
		Vec3d[] compare = HIT_VECTORS[getSide().ordinal()];
		int closestFace = -1;
		double distance = Double.MAX_VALUE;
		for (int i = 0; i < 4; i++) {
			double d = compare[i].distanceTo(vec);
			if (d < distance) {
				closestFace = i;
				distance = d;
			}
		}

		if (closestFace >= 0) {
			EnumFacing dir = EnumFacing.getFront(closestFace + 2);
			EnumFacing itop = top;
			while (itop != EnumFacing.NORTH) {
				dir = dir.rotateYCCW();
				itop = itop.rotateYCCW();
			}
			return dir;
		} else {
			return null;
		}
	}

	public boolean onActivated(EntityPlayer playerIn, ItemStack stack, Vec3d vec) {
		boolean changed = false;
		boolean remote = getWorld().isRemote;

		if (stack != null) {
			if (stack.getItem() instanceof ItemBlock) {
				Block block = Block.getBlockFromItem(stack.getItem());
				if (block == Blocks.REDSTONE_TORCH || block == Blocks.UNLIT_REDSTONE_TORCH) {
					EnumFacing closestFace = getClosestFace(vec.subtract(getPos().getX(), getPos().getY(), getPos().getZ()));

					if (closestFace != null) {
						if (canInvertSide(closestFace) && !isSideInverted(closestFace)) {
							if (!remote) {
								invertedSides |= (1 << (closestFace.ordinal() - 2));
								stack.stackSize--;
							}
							changed = true;
						}
					}
				}
			}
		} else {
			EnumFacing closestFace = getClosestFace(vec.subtract(getPos().getX(), getPos().getY(), getPos().getZ()));

			if (closestFace != null) {
				if (canInvertSide(closestFace) && isSideInverted(closestFace)) {
					if (!remote) {
						invertedSides &= ~(1 << (closestFace.ordinal() - 2));
						ItemUtils.spawnItemEntity(getWorld(), vec.xCoord, vec.yCoord, vec.zCoord,
								new ItemStack(Blocks.REDSTONE_TORCH), 0.0f, 0.2f, 0.0f, 0.1f);
					}
					changed = true;
				} else if (playerIn.isSneaking()) {
					if (closestFace != null) {
						if (canBlockSide(closestFace)) {
							if (!remote) {
								enabledSides ^= (1 << (closestFace.ordinal() - 2));
							}
							changed = true;
						}
					}
				}
			}
		}

		if (changed) {
			if (!remote) {
				notifyBlockUpdate();
				onChanged();
				sendUpdatePacket();
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onActivated(EntityPlayer playerIn, EnumHand hand, ItemStack stack, RayTraceResult hit) {
		return onActivated(playerIn, stack, hit.hitVec);
	}

	@Override
	public float getHardness(RayTraceResult hit) {
		return 0.5F;
	}

	@Override
	public ResourceLocation getModelPath() {
		return getType();
	}

	public String getModelName() {
		return "base";
	}

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return EnumSet.of(PartSlot.getFaceSlot(side));
	}

	@Override
	public boolean canConnectRedstone(EnumFacing direction) {
		if (side.getAxis() != direction.getAxis()) {
			EnumFacing dir = realToGate(direction);
			if (dir != null && isSideOpen(dir)) {
				return getType(dir).isRedstone();
			}
		}
		return false;
	}

	@Override
	public int getWeakSignal(EnumFacing facing) {
		EnumFacing dir = realToGate(facing);
		if (dir != null && getType(dir).isOutput() && getType(dir).isRedstone() && isSideOpen(dir)) {
			return getValueOutside(dir);
		} else {
			return 0;
		}
	}

	@Override
	public int getStrongSignal(EnumFacing facing) {
		return 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setByteArray("v", values);
		tag.setByte("e", enabledSides);
		tag.setByte("i", invertedSides);
		tag.setByte("f", (byte) side.ordinal());
		tag.setByte("t", (byte) top.ordinal());
		tag.setBoolean("m", mirrored);
		if (pendingTick != 0) {
			tag.setByte("p", (byte) pendingTick);
		}
		return tag;
	}

	public void readItemNBT(NBTTagCompound tag) {
		if (tag.hasKey("e")) {
			enabledSides = tag.getByte("e");
		}
		if (tag.hasKey("i")) {
			invertedSides = tag.getByte("i");
		}
	}

	public void writeItemNBT(NBTTagCompound tag, boolean silky) {
		if (silky) {
			tag.setByte("e", enabledSides);
		}
		tag.setByte("i", invertedSides);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		if (tag.hasKey("v")) {
			values = tag.getByteArray("v");
			pendingTick = tag.getByte("p");
		} else {
			if (tag.hasKey("in")) {
				values = tag.getByteArray("in");
			} else {
				values = null;
			}
			pendingTick = 2;
		}

		if (values == null || values.length != 4) {
			values = new byte[4];
		}

		enabledSides = tag.getByte("e");
		invertedSides = tag.getByte("i");
		side = EnumFacing.getFront(tag.getByte("f"));
		top = EnumFacing.getFront(tag.getByte("t"));
		mirrored = tag.getBoolean("m");
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		buf.writeByte((mirrored ? 0x40 : 0) | (side.ordinal() << 3) | top.ordinal());
		buf.writeByte(enabledSides | (invertedSides << 4));
		for (int i = 0; i < 4; i++) {
			EnumFacing facing = EnumFacing.getFront(i + 2);
			if (isSideOpen(facing)) {
				buf.writeByte(values[i]);
			}
		}
	}

	public void handlePacket(ByteBuf buf) {
		int sides = buf.readUnsignedByte();
		enabledSides = (byte) (sides & 15);
		invertedSides = (byte) (sides >> 4);
		for (int i = 0; i < 4; i++) {
			EnumFacing facing = EnumFacing.getFront(i + 2);
			if (isSideOpen(facing)) {
				values[i] = buf.readByte();
			}
		}
		markRenderUpdate();
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		int sides = buf.readUnsignedByte();
		side = EnumFacing.getFront((sides >> 3) & 7);
		top = EnumFacing.getFront((sides & 7));
		mirrored = (sides & 0x40) != 0;

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
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		AxisAlignedBB box = BOXES[side.ordinal()];
		if (box != null && box.intersectsWith(mask)) {
			list.add(box);
		}
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		AxisAlignedBB box = BOXES[side.ordinal()];
		if (box != null) {
			list.add(box);
		}
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		AxisAlignedBB box = BOXES[side.ordinal()];
		if (box != null) {
			list.add(box);
		}
	}

	public static final Property PROPERTY = new Property();

	@Override
	public IBlockState getExtendedState(IBlockState state) {
		return ((IExtendedBlockState) state).withProperty(PROPERTY, this);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new ExtendedBlockState(ForgeMultipart.multipart, new IProperty[0], new IUnlistedProperty[]{PROPERTY});
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	private final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][]{
			{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}
	};

	public boolean rsToDigi(byte v) {
		return v > 0;
	}

	public byte digiToRs(boolean v) {
		return v ? (byte) 15 : 0;
	}

	public EnumFacing gateToReal(EnumFacing dir) {
		if (dir.getAxis() == EnumFacing.Axis.Y) {
			return null;
		}

		if (dir.getAxis() == EnumFacing.Axis.X && mirrored) {
			dir = dir.getOpposite();
		}

		EnumFacing itop = top;
		while (itop != EnumFacing.NORTH) {
			dir = dir.rotateY();
			itop = itop.rotateYCCW();
		}

		return CONNECTION_DIRS[side.ordinal()][dir.ordinal() - 2];
	}

	public EnumFacing realToGate(EnumFacing rdir) {
		if (rdir.getAxis() == side.getAxis()) {
			return null;
		}

		for (int i = 0; i < 4; i++) {
			if (CONNECTION_DIRS[side.ordinal()][i] == rdir) {
				EnumFacing dir = EnumFacing.getFront(i + 2);
				EnumFacing itop = top;
				while (itop != EnumFacing.NORTH) {
					dir = dir.rotateYCCW();
					itop = itop.rotateYCCW();
				}
				if (dir.getAxis() == EnumFacing.Axis.X && mirrored) {
					return dir.getOpposite();
				} else {
					return dir;
				}
			}
		}

		return null;
	}

	private int getUniqueSideRenderID(EnumFacing side) {
		return (isSideInverted(side) ? 16 : 0) | (isSideOpen(side) ? 32 : 0) | getValueInside(side);
	}

	@Override
	public boolean renderEquals(PartGate other) {
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}

		if (this.getSide() != other.getSide() || this.getTop() != this.getTop() || this.isMirrored() != other.isMirrored()) {
			return false;
		}

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (getUniqueSideRenderID(facing) != other.getUniqueSideRenderID(facing)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int renderHashCode() {
		return Objects.hash(this.getClass(), this.getSide(), this.getTop(), this.isMirrored(),
				getUniqueSideRenderID(EnumFacing.NORTH), getUniqueSideRenderID(EnumFacing.SOUTH), getUniqueSideRenderID(EnumFacing.WEST), getUniqueSideRenderID(EnumFacing.EAST));
	}
}
