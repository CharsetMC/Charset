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

package pl.asie.simplelogic.gates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.wires.*;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.misc.ISimpleLogicSidedEmitter;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.component.NotificationComponent;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.stagingapi.ISignalMeterData;
import pl.asie.charset.lib.stagingapi.ISignalMeterDataProvider;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.Orientation;
import pl.asie.charset.lib.utils.Quaternion;
import pl.asie.charset.lib.utils.RotationUtils;
import pl.asie.charset.lib.utils.redstone.RedstoneUtils;
import pl.asie.charset.lib.wires.*;
import pl.asie.simplelogic.gates.logic.*;
import pl.asie.simplelogic.wires.logic.PartWireInsulated;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class PartGate extends TileBase implements IDebuggable, IGateContainer, IRenderComparable<PartGate>, ISignalMeterDataProvider, ITickable {
	private class RedstoneCommunications implements IBundledEmitter, IBundledReceiver, IRedstoneEmitter, IRedstoneReceiver, ISimpleLogicSidedEmitter {
		private final EnumFacing side;

		RedstoneCommunications(EnumFacing side) {
			this.side = side;
		}

		@Override
		public byte[] getBundledSignal() {
			GateConnection type = logic.getType(side);
			return type.isOutput() && type.isBundled() ? logic.getOutputValueBundled(side) : new byte[0];
		}

		@Override
		public void onBundledInputChange() {
			onChanged();
		}

		@Override
		public int getRedstoneSignal() {
			GateConnection type = logic.getType(side);
			return type.isOutput() && type.isRedstone() ? logic.getOutputValueOutside(side) : 0;
		}

		@Override
		public void onRedstoneInputChange() {
			onChanged();
		}

		@Override
		public EnumFacing getEmitterFace() {
			return PartGate.this.orientation.facing.getOpposite();
		}
	}

	public static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];
	private static final Vec3d[] HIT_VECTORS;
	private final RedstoneCommunications[] COMMS = new RedstoneCommunications[4];

	static {
		for (int i = 0; i < 6; i++) {
			EnumFacing facing = EnumFacing.byIndex(i);
			BOXES[i] = RotationUtils.rotateFace(new AxisAlignedBB(0, 0, 0, 1, 0.125, 1), facing);
		}

		HIT_VECTORS = new Vec3d[5];
		HIT_VECTORS[0] = new Vec3d(0.5, 0.125, 0);
		HIT_VECTORS[1] = new Vec3d(0.5, 0.125, 1);
		HIT_VECTORS[2] = new Vec3d(0, 0.125, 0.5);
		HIT_VECTORS[3] = new Vec3d(1, 0.125, 0.5);
		HIT_VECTORS[4] = new Vec3d(0.5, 0.125, 0.5);
	}

	public boolean mirrored;
	public GateLogic logic;
	private long tickScheduleTime = -1;
	private long pendingTick = -1;

	private Orientation orientation = Orientation.FACE_UP_POINT_NORTH;

	public Orientation getOrientation() {
		return orientation;
	}

	public PartGate(GateLogic logic) {
		this.logic = logic;

		COMMS[0] = new RedstoneCommunications(EnumFacing.NORTH);
		COMMS[1] = new RedstoneCommunications(EnumFacing.SOUTH);
		COMMS[2] = new RedstoneCommunications(EnumFacing.WEST);
		COMMS[3] = new RedstoneCommunications(EnumFacing.EAST);
	}

	public PartGate() {
		this(new GateLogicDummy());
	}

	PartGate setInvertedSides(int invertedSides) {
		logic.invertedSides = (byte) (invertedSides & 0x0F);
		return this;
	}

	private boolean hasRedstoneCapability(Capability<?> capability, EnumFacing direction) {
		if (direction != null && direction.getAxis() != orientation.facing.getAxis()) {
			EnumFacing dir = realToGate(direction);
			if (logic.isSideOpen(dir)) {
				GateConnection conn = logic.getType(dir);

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
	public void rotate(Rotation rotationIn) {
		super.rotate(rotationIn);
		for (int i = 0; i < rotationIn.ordinal(); i++) {
			orientation = orientation.rotateAround(EnumFacing.UP);
		}
		onChanged();
		scheduleTick(0);
		markBlockForUpdate();
	}

	@Override
	public void mirror(Mirror mirror) {
		super.mirror(mirror);
		if (orientation.facing.getAxis() == EnumFacing.Axis.Y && logic.canMirror()) {
			switch (mirror) {
				case LEFT_RIGHT:
					if (orientation.top.getAxis() == EnumFacing.Axis.Z) {
						mirrored = !mirrored;
					} else {
						orientation = orientation.getNextRotationOnFace().getNextRotationOnFace();
						mirrored = !mirrored;
					}
					break;
				case FRONT_BACK:
					if (orientation.top.getAxis() == EnumFacing.Axis.X) {
						mirrored = !mirrored;
					} else {
						orientation = orientation.getNextRotationOnFace().getNextRotationOnFace();
						mirrored = !mirrored;
					}
					break;
			}
			onChanged();
			scheduleTick(0);
			markBlockForUpdate();
		} else {
			orientation = orientation.mirror(mirror);
			onChanged();
			scheduleTick(0);
			markBlockForUpdate();
		}
	}

	public void openGUI(EntityPlayer player) {
		SimpleLogicGates.proxy.openGui(this, player);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing direction) {
		return capability == Capabilities.DEBUGGABLE || capability == Capabilities.SIGNAL_METER_DATA_PROVIDER
				|| (capability == SimpleLogicGates.GATE_CAP && direction == getSide()) || hasRedstoneCapability(capability, direction)
				|| (logic instanceof ICapabilityProvider && ((ICapabilityProvider) logic).hasCapability(capability, direction)) || super.hasCapability(capability, direction);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing direction) {
		if (capability == Capabilities.DEBUGGABLE || capability == SimpleLogicGates.GATE_CAP || capability == Capabilities.SIGNAL_METER_DATA_PROVIDER) {
			return (T) this;
		} else if (hasRedstoneCapability(capability, direction)) {
			EnumFacing dir = realToGate(direction);
			if (dir.ordinal() >= 2) {
				return (T) COMMS[dir.ordinal() - 2];
			} else {
				return null;
			}
		} else if (logic instanceof ICapabilityProvider && ((ICapabilityProvider) logic).hasCapability(capability, direction)) {
			return ((ICapabilityProvider) logic).getCapability(capability, direction);
		} else {
			return super.getCapability(capability, direction);
		}
	}

	@Override
	public ItemStack getPickedBlock(@Nullable EntityPlayer player, @Nullable RayTraceResult result, IBlockState state) {
		ItemStack stack = ItemGate.getStack(this, true);
		saveToStack(stack);
		return stack;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> stacks, IBlockState state, int fortune, boolean silkTouch) {
		ItemStack stack = ItemGate.getStack(this, silkTouch);
		saveToStack(stack);
		stacks.add(stack);
	}

	@Override
	public void update() {
		//noinspection ConstantConditions
		if (getWorld() != null) {
			if (logic instanceof IGateTickable) {
				((IGateTickable) logic).update(this);
			}

			if (!getWorld().isRemote) {
				if (pendingTick >= 0 && world.getTotalWorldTime() >= pendingTick && logic.shouldTick()) {
					pendingTick = -1;
					if (logic.tick(this)) {
						markGateChanged(logic.updateOutputs(this));
					}
					if (logic.updateInputs(this)) {
						logic.onChanged(this);
					}
				}
			}
		}
	}

	@Override
	public byte getRedstoneInput(EnumFacing facing) {
		GateConnection conn = logic.getType(facing);

		if (conn == GateConnection.INPUT_REPEATER) {
			EnumFacing real = gateToReal(facing);
			World w = getWorld();
			BlockPos p = getPos().offset(real);
			Predicate<TileEntity> predicate = tileEntity -> (tileEntity instanceof PartGate && ((PartGate) tileEntity).logic instanceof GateLogicRepeater);
			return (byte) MathHelper.clamp(RedstoneUtils.getModdedWeakPower(w, p, real, getSide(), predicate), 0, 15);
		} else if (conn.isInput() && conn.isRedstone()) {
			byte v = 0;
			if (logic.isSideOpen(facing)) {
				EnumFacing real = gateToReal(facing);
				World w = getWorld();
				BlockPos p = getPos().offset(real);
				Predicate<TileEntity> predicate = tileEntity -> true;

				int mpValue = RedstoneUtils.getModdedWeakPower(w, p, real, getSide(), predicate);
				if (mpValue >= 0) {
					v = (byte) mpValue;
				} else {
					TileEntity tile = w.getTileEntity(p);
					if (tile != null && tile.hasCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite())) {
						IRedstoneEmitter emitter = tile.getCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite());
						if (!(emitter instanceof ISimpleLogicSidedEmitter) || ((ISimpleLogicSidedEmitter) emitter).getEmitterFace() == getOrientation().facing.getOpposite()) {
							v = (byte) emitter.getRedstoneSignal();
						}
					} else {
						IBlockState s = w.getBlockState(p);
						if (RedstoneUtils.canConnectFace(w, p, s, real, getSide())) {
							if (s.getBlock() instanceof BlockRedstoneWire) {
								v = s.getValue(BlockRedstoneWire.POWER).byteValue();
							} else {
								v = (byte) (byte) s.getWeakPower(w, p, real);
							}
						}
					}
				}

				if (conn.isComparator() && v < 15) {
					IBlockState s = w.getBlockState(p);
					if (s.hasComparatorInputOverride()) {
						v = (byte) Math.max(MathHelper.clamp(s.getComparatorInputOverride(w, p), 0, 15), v);
					} else if (s.isNormalCube()) {
						v = 0;
						BlockPos pp = p.offset(real);
						s = w.getBlockState(pp);
						if (s.hasComparatorInputOverride()) {
							v = (byte) Math.max(MathHelper.clamp(s.getComparatorInputOverride(w, pp), 0, 15), v);
						} else if (!s.isNormalCube()) {
							List<EntityItemFrame> frames = world.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(pp), (frame) -> frame != null && frame.getHorizontalFacing() == facing);
							for (EntityItemFrame frame : frames) {
								v = (byte) Math.max(MathHelper.clamp(frame.getAnalogOutput(), 0, 15), v);
							}
						}
					}
				}

				if (conn.isDigital()) {
					v = v != 0 ? (byte) 15 : 0;
				}

				if (logic.isSideInverted(facing)) {
					v = v != 0 ? 0 : (byte) 15;
				}
			}

			return v;
		} else {
			return 0;
		}
	}

	@Override
	public byte[] getBundledInput(EnumFacing facing) {
		GateConnection conn = logic.getType(facing);

		if (conn.isInput() && conn.isBundled()) {
			if (logic.isSideOpen(facing)) {
				EnumFacing real = gateToReal(facing);
				World w = getWorld();
				BlockPos p = getPos().offset(real);
				byte[] mpValue = RedstoneUtils.getModdedBundledPower(w, p, real, getSide(), (t) -> true);
				if (mpValue != null) {
					return mpValue;
				} else {
					TileEntity tile = w.getTileEntity(p);
					if (tile != null) {
						if (tile.hasCapability(Capabilities.BUNDLED_EMITTER, real.getOpposite())) {
							IBundledEmitter emitter = tile.getCapability(Capabilities.BUNDLED_EMITTER, real.getOpposite());
							if (!(emitter instanceof ISimpleLogicSidedEmitter) || ((ISimpleLogicSidedEmitter) emitter).getEmitterFace() == getOrientation().facing.getOpposite()) {
								return emitter.getBundledSignal();
							}
						}
					}
				}
			}
		}

		return new byte[16];
	}

	@Override
	public Notice createNotice(NotificationComponent component) {
		return new Notice(this, component);
	}

	@Override
	public void markGateChanged(boolean changedIO) {
		if (changedIO) {
//			System.out.println("change " + pos + " " + getWorld().getTotalWorldTime());
			world.notifyNeighborsRespectDebug(getPos(), getBlockType(), false);
		}

		if (world.isRemote) {
			if (!SimpleLogicGates.useTESRs) {
				markBlockForRenderUpdate();
			}
		} else {
			markChunkDirty();
			markBlockForUpdate();
		}
	}

	public boolean getInverterState(EnumFacing facing) {
		byte value = logic.getType(facing).isInput() ? logic.getInputValueOutside(facing) : logic.getOutputValueInside(facing);
		return value == 0;
	}

	protected void onChanged() {
		if ((pendingTick < 0 || tickScheduleTime == world.getTotalWorldTime()) && logic.updateInputs(this)) {
			logic.onChanged(this);
		}
	}

	@Override
	public World getGateWorld() {
		return world;
	}

	@Override
	public BlockPos getGatePos() {
		return pos;
	}

	public void scheduleTick(int duration) {
		if (pendingTick < 0) {
			pendingTick = world.getTotalWorldTime() + duration;
			tickScheduleTime = world.getTotalWorldTime();
		} else {
			pendingTick = Math.min(pendingTick, world.getTotalWorldTime() + duration);
		}
	}


	@Override
	public void validate() {
		super.validate();
		scheduleTick(0);
	}

	public void onNeighborBlockChange(BlockPos fromPos, @Nullable Block block) {
		if (logic instanceof GateLogicDummy || !getWorld().isSideSolid(getPos().offset(getSide()), getSide().getOpposite())) {
			IBlockState state = world.getBlockState(pos);
			state.getBlock().dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
			return;
		}

		onChanged();
	}

	public boolean rotate(EnumFacing axis) {
		if (axis.getAxis() == orientation.facing.getAxis()) {
			if (axis.getAxisDirection() == orientation.facing.getAxisDirection()) {
				orientation = orientation.getNextRotationOnFace();
			} else {
				orientation = orientation.getPrevRotationOnFace();
			}

			onChanged();
			markBlockForUpdate();
			return true;
		}
		return false;
	}

	@Nullable
	private EnumFacing getClosestFace(Vec3d vec, Predicate<EnumFacing> predicate) {
		EnumFacing closestFace = null;
		double distance = Double.MAX_VALUE;
		for (int i = 0; i <= 4; i++) {
			double d = HIT_VECTORS[i].squareDistanceTo(vec);
			if (d < distance) {
				EnumFacing face = i == 4 ? null : EnumFacing.byIndex(i + 2);
				if (predicate.test(face)) {
					closestFace = face;
					distance = d;
				}
			}
		}

		return closestFace;
	}

	public boolean onActivated(EntityPlayer playerIn, EnumHand hand, float hitX, float hitY, float hitZ) {
		boolean changed = false;
		boolean used = false;
		boolean remote = getWorld().isRemote;
		ItemStack stack = playerIn.getHeldItem(hand);
		Vec3d vecOrig = new Vec3d(hitX, hitY, hitZ);
		Vec3d vec = realToGate(vecOrig);

		if (!stack.isEmpty()) {
			if (stack.getItem().getToolClasses(stack).contains("wrench")) {
				if (playerIn.isSneaking()) {
					if (logic.canMirror()) {
						mirrored = !mirrored;
						changed = true;
					}
				} else {
					changed = rotate(orientation.facing);
				}

				used = true;
			} else if (stack.getItem() instanceof ItemBlock) {
				Block block = Block.getBlockFromItem(stack.getItem());
				if (block == Blocks.REDSTONE_TORCH || block == Blocks.UNLIT_REDSTONE_TORCH) {
					EnumFacing closestFace = getClosestFace(vec, facing -> true);

					if (closestFace != null) {
						if (logic.canInvertSide(closestFace) && !logic.isSideInverted(closestFace)) {
							if (!remote) {
								logic.invertedSides |= (1 << (closestFace.ordinal() - 2));
								if (!playerIn.isCreative()) {
									stack.shrink(1);
								}
							}
							changed = true;
						}
					}

					used = true;
				}
			}
		}

		if (!used) {
			EnumFacing closestFace = getClosestFace(vec, facing -> true);

			if (closestFace != null) {
				if (logic.canInvertSide(closestFace) && logic.isSideInverted(closestFace)) {
					if (!remote) {
						logic.invertedSides &= ~(1 << (closestFace.ordinal() - 2));
						ItemUtils.spawnItemEntity(getWorld(), vecOrig.add(getPos().getX(), getPos().getY(), getPos().getZ()),
								new ItemStack(Blocks.REDSTONE_TORCH), 0.0f, 0.2f, 0.0f, 0.1f);
					}
					changed = true;
				} else if (playerIn.isSneaking()) {
					if (logic.canBlockSide(closestFace)) {
						if (!remote) {
							logic.enabledSides ^= (1 << (closestFace.ordinal() - 2));
						}
						changed = true;
					}
				}
			}

			if (!changed) {
				changed = logic.onRightClick(this, playerIn, vec, hand);
			}
		}

		if (changed) {
			logic.updateInputs(this);
			logic.onChanged(this);
			return true;
		} else {
			return false;
		}
	}

	public String getBaseModelName() {
		return mirrored ? "base_mirrored" : "base";
	}

	public String getLayerModelName() {
		return mirrored ? "layer_mirrored" : "layer";
	}

	public boolean canConnectRedstone(@Nullable EnumFacing direction) {
		if (direction != null && orientation.facing.getAxis() != direction.getAxis()) {
			EnumFacing dir = realToGate(direction);
			if (dir != null && logic.isSideOpen(dir)) {
				return logic.getType(dir).isRedstone();
			}
		}

		return false;
	}

	public int getWeakSignal(EnumFacing facing) {
		EnumFacing dir = realToGate(facing);
		if (dir != null && logic.getType(dir).isOutput() && logic.getType(dir).isRedstone() && logic.isSideOpen(dir)) {
			return logic.getOutputValueOutside(dir);
		} else {
			return 0;
		}
	}

	@Override
	public void onPlacedBy(EntityLivingBase placer, @Nullable EnumFacing face, ItemStack stack, float hitX, float hitY, float hitZ) {
		super.onPlacedBy(placer, face, stack, hitX, hitY, hitZ);
		if (face != null) {
			orientation = Orientation.fromDirection(SimpleLogicGates.onlyBottomFace ? EnumFacing.UP : face);
			Vec3d vec = realToGate(new Vec3d(hitX, hitY, hitZ));
			orientation = orientation.pointTopTo(gateToReal(getClosestFace(vec, Objects::nonNull)));
		}
		if (logic != null) {
			logic.updateOutputs(this);
		}
	}

	@Override
	public NBTTagCompound writeNBTData(NBTTagCompound tag, boolean isClient) {
		if (!(logic instanceof GateLogicDummy)) {
			tag.setString("logic", SimpleLogicGates.logicClasses.inverse().get(logic.getClass()).toString());
			tag = logic.writeToNBT(tag, isClient);
		}
		tag.setBoolean("m", mirrored);
		tag.setByte("o", (byte) orientation.ordinal());
		if (!isClient) {
			if (pendingTick >= 0) {
				tag.setLong("pt", pendingTick);
			}
		}
		return tag;
	}

	@Override
	public void loadFromStack(ItemStack stack) {
		super.loadFromStack(stack);
		if (stack.hasTagCompound()) {
			readItemNBT(stack.getTagCompound());
		}
	}

	public void readItemNBT(NBTTagCompound tag) {
		if (tag.hasKey("logic", Constants.NBT.TAG_STRING)) {
			Optional<GateLogic> logic = ItemGate.getGateLogic(new ResourceLocation(tag.getString("logic")));
			logic.ifPresent((a) -> a.readFromNBT(tag, false));
			this.logic = logic.orElseGet(GateLogicDummy::new);
		}
	}

	public NBTTagCompound writeItemNBT(NBTTagCompound tag, boolean silky) {
		if (!(logic instanceof GateLogicDummy)) {
			tag.setString("logic", SimpleLogicGates.logicClasses.inverse().get(logic.getClass()).toString());
			return logic.writeItemNBT(tag, silky);
		} else {
			return tag;
		}
	}

	@Override
	public void readNBTData(NBTTagCompound tag, boolean isClient) {
		boolean renderUpdate = false;
		boolean orientationUpdate = false;

		if (tag.hasKey("logic", Constants.NBT.TAG_STRING)) {
			Optional<GateLogic> logic = ItemGate.getGateLogic(this.logic, new ResourceLocation(tag.getString("logic")));
			if (logic.isPresent()) {
				renderUpdate |= logic.get().readFromNBT(tag, isClient);
			}
			this.logic = logic.orElseGet(GateLogicDummy::new);
		}
		if (tag.hasKey("m")) {
			boolean om = mirrored;
			mirrored = tag.getBoolean("m");
			orientationUpdate |= (mirrored != om);
		}
		if (!isClient) {
			if (tag.hasKey("pt")) {
				pendingTick = tag.getLong("pt");
			} else {
				pendingTick = -1;
			}
		}
		Orientation oldO = orientation;
		orientation = Orientation.getOrientation(tag.getByte("o"));
		orientationUpdate |= (oldO != orientation);

		if (isClient && (orientationUpdate || (!SimpleLogicGates.useTESRs && renderUpdate))) {
			markBlockForRenderUpdate();
		}
	}

	public EnumFacing getSide() {
		return orientation.facing.getOpposite();
	}

	public EnumFacing getTop() {
		EnumFacing[] f = CONNECTION_DIRS[getSide().ordinal()];

		for (int i = 0; i < 4; i++) {
			if (orientation.top == f[i]) {
				return EnumFacing.byIndex(i + 2);
			}
		}

		throw new RuntimeException("!?");
	}

	@Override
	public GateLogic getLogic() {
		return logic;
	}

	public AxisAlignedBB getBox() {
		return BOXES[getSide().ordinal()];
	}

	private final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][]{
			{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}
	};

	protected EnumFacing gateToReal(EnumFacing dir) {
		if (dir.getAxis() == EnumFacing.Axis.Y) {
			return null;
		}

		if (dir.getAxis() == EnumFacing.Axis.X && mirrored) {
			dir = dir.getOpposite();
		}

		EnumFacing itop = getTop();
		while (itop != EnumFacing.NORTH) {
			dir = dir.rotateY();
			itop = itop.rotateYCCW();
		}

		return CONNECTION_DIRS[getSide().ordinal()][dir.ordinal() - 2];
	}

	protected Vec3d realToGate(Vec3d vec) {
		return Quaternion.fromOrientation(orientation.getPrevRotationOnFace()).applyReverseRotation(vec.scale(2).subtract(1, 1, 1)).add(1, 1, 1).scale(0.5);
	}

	protected EnumFacing realToGate(EnumFacing rdir) {
		if (rdir.getAxis() == orientation.facing.getAxis()) {
			return null;
		}

		for (int i = 0; i < 4; i++) {
			if (CONNECTION_DIRS[getSide().ordinal()][i] == rdir) {
				EnumFacing dir = EnumFacing.byIndex(i + 2);
				EnumFacing itop = getTop();
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
		return (logic.isSideInverted(side) ? 16 : 0) | (logic.isSideOpen(side) ? 32 : 0) | (logic.getInputValueInside(side) << 6) | logic.getOutputValueInside(side);
	}

	@Override
	public boolean renderEquals(PartGate other) {
		if (logic.getClass() != other.logic.getClass()) {
			return false;
		}

		if (this.orientation != other.orientation || this.mirrored != other.mirrored) {
			return false;
		}

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (getUniqueSideRenderID(facing) != other.getUniqueSideRenderID(facing)) {
				return false;
			}
		}

		if (!logic.renderEquals(other.logic)) {
			return false;
		}

		return true;
	}

	@Override
	public int renderHashCode() {
		return logic.renderHashCode(Objects.hash(logic.getClass(), this.orientation, this.mirrored,
				getUniqueSideRenderID(EnumFacing.NORTH), getUniqueSideRenderID(EnumFacing.SOUTH), getUniqueSideRenderID(EnumFacing.WEST), getUniqueSideRenderID(EnumFacing.EAST)));
	}

	@Override
	public void addDebugInformation(List<String> stringList, Side side) {
		if (side == Side.SERVER) {
			stringList.add("O: " + getOrientation().name() + (mirrored ? TextFormatting.RED+"M" : ""));
		}

		if (logic instanceof IDebuggable) {
			((IDebuggable) logic).addDebugInformation(stringList, side);
		}
	}

	@Override
	public ISignalMeterData getSignalMeterData(RayTraceResult result) {
		Vec3d vecOrig = result.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());
		Vec3d vec = realToGate(vecOrig);
		EnumFacing facing = getClosestFace(vec, facing1 -> {
			if (facing1 == null) {
				return true;
			} else {
				return logic.getType(facing1).isInput() || logic.getType(facing1).isOutput();
			}
		});

		if (facing == null) {
			if (logic instanceof ISignalMeterDataProvider) {
				return ((ISignalMeterDataProvider) logic).getSignalMeterData(result);
			}

			facing = getClosestFace(vec, facing1 -> {
				if (facing1 == null) {
					return false;
				} else {
					return logic.getType(facing1).isInput() || logic.getType(facing1).isOutput();
				}
			});
		}

		GateConnection type = logic.getType(facing);
		if (type.isInput() && type.isBundled()) {
			return new SignalMeterDataBundledWire(logic.getInputValueBundled(facing));
		} else if (type.isInput() && type.isRedstone()) {
			return new SignalMeterDataWire(logic.getInputValueInside(facing), -1);
		} else if (type.isOutput() && type.isBundled()) {
			return new SignalMeterDataBundledWire(logic.getOutputValueBundled(facing));
		} else if (type.isOutput() && type.isRedstone()) {
			return new SignalMeterDataWire(logic.getOutputValueInside(facing), -1);
		}

		return null;
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}
}
