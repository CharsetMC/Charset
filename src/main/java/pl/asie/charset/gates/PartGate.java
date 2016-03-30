package pl.asie.charset.gates;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mcmultipart.multipart.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
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
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import mcmultipart.MCMultiPartMod;
import mcmultipart.capabilities.ISlottedCapabilityProvider;
import mcmultipart.raytrace.PartMOP;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.render.IRenderComparable;
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
			return getOutputOutside(side);
		}

		@Override
		public void onRedstoneInputChange() {
			onChanged();
		}
	}

	private static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];
	private final RedstoneCommunications[] COMMS = new RedstoneCommunications[4];

	static {
		for (int i = 0; i < 6; i++) {
			BOXES[i] = RotationUtils.rotateFace(new AxisAlignedBB(0, 0, 0, 1, 0.125, 1), EnumFacing.getFront(i));
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
	private byte[] inputs = new byte[4];
	private byte[] outputClient = new byte[4];

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
	public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {
		return ItemGate.getStack(this, true);
	}

	@Override
	public List<ItemStack> getDrops() {
		return Arrays.asList(ItemGate.getStack(this, false));
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
		byte[] oldOutput = new byte[4];
		boolean changed = false;

		for (int i = 0; i <= 3; i++) {
			Connection conn = getType(side);
			if (conn.isOutput() && conn.isRedstone()) {
				oldOutput[i] = getOutputOutside(side);
			}
		}
		for (int i = 0; i <= 3; i++) {
			EnumFacing side = EnumFacing.getFront(i + 2);
			Connection conn = getType(side);
			if (conn.isInput() && conn.isRedstone()) {
				EnumFacing real = gateToReal(side);
				byte oi = inputs[i];
				World w = getWorld();
				BlockPos p = getPos().offset(real);
				IMultipartContainer container = MultipartHelper.getPartContainer(w, p);
				if (container != null) {
					inputs[i] = (byte) MultipartRedstoneHelper.getWeakSignal(container, real.getOpposite(), this.side);
				} else {
					TileEntity tile = w.getTileEntity(p);
					if (tile != null && tile.hasCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite())) {
						inputs[i] = (byte) tile.getCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite()).getRedstoneSignal();
					} else {
						IBlockState s = w.getBlockState(p);
						if (RedstoneUtils.canConnectFace(w, p, s, real, this.side)) {
							if (s.getBlock() instanceof BlockRedstoneWire) {
								inputs[i] = s.getValue(BlockRedstoneWire.POWER).byteValue();
							} else {
								inputs[i] = (byte) s.getBlock().getWeakPower(s, w, p, real);
							}
						}
					}
				}

				if (conn.isDigital()) {
					inputs[i] = inputs[i] != 0 ? (byte) 15 : 0;
				}
				if (inputs[i] != oi) {
					changed = true;
				}
			}
		}

		if (!changed) {
			for (int i = 0; i <= 3; i++) {
				Connection conn = getType(side);
				if (conn.isOutput() && conn.isRedstone()) {
					if (getOutputOutside(side) != oldOutput[i]) {
						changed = true;
						break;
					}
				}
			}
		}

		return changed;
	}

	public byte getInputOutside(EnumFacing side) {
		return inputs[side.ordinal() - 2];
	}

	protected byte getInputInside(EnumFacing side) {
		if (isSideInverted(side)) {
			return inputs[side.ordinal() - 2] != 0 ? 0 : (byte) 15;
		} else {
			return inputs[side.ordinal() - 2];
		}
	}

	public boolean getInverterState(EnumFacing facing) {
		byte value = getType(facing).isInput() ? getInputOutside(facing) : getOutputInsideClient(facing);
		return value == 0;
	}

	protected byte getOutputOutside(EnumFacing side) {
		if (isSideInverted(side)) {
			return getOutputInside(side) != 0 ? 0 : (byte) 15;
		} else {
			return (byte) getOutputInside(side);
		}
	}

	public byte getOutputInsideClient(EnumFacing side) {
		return outputClient[side.ordinal() - 2];
	}


	public byte getOutputOutsideClient(EnumFacing side) {
		if (isSideInverted(side)) {
			return outputClient[side.ordinal() - 2] != 0 ? 0 : (byte) 15;
		} else {
			return outputClient[side.ordinal() - 2];
		}
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
	public EnumFacing[] getValidRotations() {
		return new EnumFacing[]{side, side.getOpposite()};
	}

	@Override
	public boolean rotatePart(EnumFacing axis) {
		if (axis.getAxis() == side.getAxis()) {
			if (axis.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
				top = top.rotateY();
			} else {
				top = top.rotateYCCW();
			}
			return true;
		}
		return false;
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

	protected abstract byte getOutputInside(EnumFacing side);

	public boolean isSideOpen(EnumFacing side) {
		return (enabledSides & (1 << (side.ordinal() - 2))) != 0;
	}

	public boolean isSideInverted(EnumFacing side) {
		return (invertedSides & (1 << (side.ordinal() - 2))) != 0;
	}

	private boolean isInvalidInverted() {
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (isSideInverted(facing) && !canInvertSide(facing)) {
				return true;
			}
		}

		return false;
	}

	private boolean isInvalidEnabled() {
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (!isSideOpen(facing) && !canBlockSide(facing)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onActivated(EntityPlayer playerIn, EnumHand hand, ItemStack stack, PartMOP hit) {
		if (!playerIn.worldObj.isRemote) {
			if (stack != null) {
				if (stack.getItem() instanceof ItemScrewdriver) {
					if (playerIn.isSneaking()) {
						top = top.rotateY();
					} else {
						int z = 32;
						enabledSides = (byte) ((enabledSides + 1) & 15);
						while (z > 0 && ((~getSideMask() & enabledSides) != 0
								|| isInvalidEnabled() || enabledSides == 0)) {
							enabledSides = (byte) ((enabledSides + 1) & 15);
							z--;
						}
						if (z == 0) {
							enabledSides = getSideMask();
						}
					}

					notifyBlockUpdate();
					onChanged();
					sendUpdatePacket();
					return true;
				}
			} else {
				/* if (playerIn.isSneaking()) {
                    int z = 32;
                    invertedSides = (byte) ((invertedSides + 1) & 15);
                    while (z > 0 && ((~getSideMask() & invertedSides) != 0
                            || isInvalidInverted() || invertedSides == 0)) {
                        invertedSides = (byte) ((invertedSides + 1) & 15);
                        z--;
                    }
                    if (z == 0) {
                        invertedSides = getSideMask();
                    }

                    notifyBlockUpdate();
                    sendUpdatePacket();
                    return true;
                } */
			}
		}
		return false;
	}

	@Override
	public float getHardness(PartMOP hit) {
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
			if (isSideOpen(dir)) {
				return getType(dir).isRedstone();
			}
		}
		return false;
	}

	@Override
	public int getWeakSignal(EnumFacing facing) {
		EnumFacing dir = realToGate(facing);
		if (getType(dir).isOutput() && getType(dir).isRedstone() && isSideOpen(dir)) {
			return getOutputOutside(dir);
		} else {
			return 0;
		}
	}

	@Override
	public int getStrongSignal(EnumFacing facing) {
		return 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		tag.setByteArray("in", inputs);
		tag.setByte("f", (byte) side.ordinal());
		tag.setByte("t", (byte) top.ordinal());
		tag.setByte("e", enabledSides);
		tag.setByte("i", invertedSides);
		tag.setBoolean("m", mirrored);
		if (pendingTick != 0) {
			tag.setByte("p", (byte) pendingTick);
		}
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
		inputs = tag.getByteArray("in");
		if (inputs == null || inputs.length != 4) {
			inputs = new byte[4];
		}
		enabledSides = tag.getByte("e");
		invertedSides = tag.getByte("i");
		side = EnumFacing.getFront(tag.getByte("f"));
		top = EnumFacing.getFront(tag.getByte("t"));
		mirrored = tag.getBoolean("m");
		pendingTick = tag.getByte("p");
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		buf.writeByte((mirrored ? 0x40 : 0) | (side.ordinal() << 3) | top.ordinal());
		buf.writeByte(enabledSides | (invertedSides << 4));
		for (int i = 0; i <= 3; i++) {
			EnumFacing dir = EnumFacing.getFront(i + 2);
			if (getType(dir) != Connection.NONE) {
				buf.writeByte(getType(dir).isInput() ? inputs[i] : getOutputInside(dir));
			}
		}
	}

	public void handlePacket(ByteBuf buf) {
		int sides = buf.readUnsignedByte();
		enabledSides = (byte) (sides & 15);
		invertedSides = (byte) (sides >> 4);

		for (int i = 0; i <= 3; i++) {
			inputs[i] = outputClient[i] = 0;
			EnumFacing dir = EnumFacing.getFront(i + 2);
			if (getType(dir) != Connection.NONE) {
				if (getType(dir).isInput()) {
					inputs[i] = buf.readByte();
				} else {
					outputClient[i] = buf.readByte();
				}
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
		return new ExtendedBlockState(MCMultiPartMod.multipart, new IProperty[0], new IUnlistedProperty[]{PROPERTY});
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
		int rid = (isSideInverted(side) ? 16 : 0) | (isSideOpen(side) ? 32 : 0);
		if (!getType(side).isBundled()) {
			rid |= getType(side) == Connection.INPUT ? getInputInside(side) : getOutputInsideClient(side);
		}
		return rid;
 	}

	@Override
	public boolean renderEquals(PartGate other) {
		if (other.getClass() != this.getClass()) {
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
