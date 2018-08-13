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

package pl.asie.simplelogic.gates;

import java.util.*;
import java.util.function.Predicate;

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
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.api.lib.IDebuggable;
import pl.asie.charset.api.wires.*;
import pl.asie.charset.lib.block.TraitMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.component.NotificationComponent;
import pl.asie.charset.lib.utils.redstone.RedstoneUtils;
import pl.asie.charset.lib.wires.TileWire;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.simplelogic.gates.logic.*;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.*;
import pl.asie.simplelogic.wires.SimpleLogicWires;

import javax.annotation.Nullable;

public class PartGate extends TileBase implements IDebuggable, IGateContainer, IRenderComparable<PartGate>, ITickable {
	private class RedstoneCommunications implements IBundledEmitter, IBundledReceiver, IRedstoneEmitter, IRedstoneReceiver {
		private final EnumFacing side;

		RedstoneCommunications(EnumFacing side) {
			this.side = side;
		}

		@Override
		public byte[] getBundledSignal() {
			GateLogic.Connection type = logic.getType(side);
			return type.isOutput() && type.isBundled() ? logic.getOutputValueBundled(side) : new byte[0];
		}

		@Override
		public void onBundledInputChange() {
			onChanged();
		}

		@Override
		public int getRedstoneSignal() {
			GateLogic.Connection type = logic.getType(side);
			return type.isOutput() && type.isRedstone() ? logic.getOutputValueOutside(side) : 0;
		}

		@Override
		public void onRedstoneInputChange() {
			onChanged();
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
	private boolean pendingChange;
	private int pendingTick;

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
				GateLogic.Connection conn = logic.getType(dir);
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
		pendingChange = true;
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
			pendingChange = true;
			markBlockForUpdate();
		} else {
			orientation = orientation.mirror(mirror);
			onChanged();
			pendingChange = true;
			markBlockForUpdate();
		}
	}

	public void openGUI(EntityPlayer player) {
		SimpleLogicGates.proxy.openGui(this, player);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing direction) {
		return capability == Capabilities.DEBUGGABLE || (capability == SimpleLogicGates.GATE_CAP && direction == getSide()) || hasRedstoneCapability(capability, direction) || super.hasCapability(capability, direction);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
		if (capability == Capabilities.DEBUGGABLE || capability == SimpleLogicGates.GATE_CAP) {
			return (T) this;
		} else if (hasRedstoneCapability(capability, enumFacing)) {
			EnumFacing dir = realToGate(enumFacing);
			if (dir.ordinal() >= 2) {
				return (T) COMMS[dir.ordinal() - 2];
			} else {
				return null;
			}
		} else {
			return super.getCapability(capability, enumFacing);
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
			if (logic instanceof ITickable) {
				((ITickable) logic).update();
			} else if (logic instanceof IGateTickable) {
				((IGateTickable) logic).update(this);
			}

			if (!getWorld().isRemote && pendingTick > 0 && logic.shouldTick()) {
				pendingTick--;
				if (pendingTick == 0) {
					if (tick() || pendingChange) {
						propagateOutputs();
						pendingChange = false;
					}
				}
			}
		}
	}

	protected boolean tick() {
		return logic.tick(this);
	}

	public void propagateOutputs() {
		world.notifyNeighborsRespectDebug(getPos(), getBlockType(), false);
		if (world.isRemote) {
			if (!SimpleLogicGates.useTESRs) {
				markBlockForRenderUpdate();
			}
		} else {
			markChunkDirty();
			markBlockForUpdate();
		}
	}

	public byte[] getBundledInput(EnumFacing facing) {
		GateLogic.Connection conn = logic.getType(facing);

		if (conn.isInput() && conn.isBundled()) {
			if (logic.isSideOpen(facing)) {
				EnumFacing real = gateToReal(facing);
				World w = getWorld();
				BlockPos p = getPos().offset(real);
				TileEntity tile = w.getTileEntity(p);
				if (tile != null && tile.hasCapability(Capabilities.BUNDLED_EMITTER, real.getOpposite())) {
					// TODO: FIXME - this is a hack
					if (!(tile instanceof TileWire) || ((TileWire) tile).getWire().getLocation().facing == getOrientation().facing) {
						return tile.getCapability(Capabilities.BUNDLED_EMITTER, real.getOpposite()).getBundledSignal();
					}
				}
			}
		}

		return new byte[16];
	}

	public boolean updateRedstoneInput(byte[] values, EnumFacing facing) {
		int i = facing.ordinal() - 2;
		GateLogic.Connection conn = logic.getType(facing);
		byte oldValue = values[i];
		values[i] = 0;

		if (conn == GateLogic.Connection.INPUT_REPEATER) {
			EnumFacing real = gateToReal(facing);
			World w = getWorld();
			BlockPos p = getPos().offset(real);
			Predicate<TileEntity> predicate = tileEntity -> (tileEntity instanceof PartGate && ((PartGate) tileEntity).logic instanceof GateLogicRepeater);
			values[i] = (byte) MathHelper.clamp(RedstoneUtils.getModdedWeakPower(w, p, real, getSide(), predicate), 0, 15);
		} else if (conn.isInput() && conn.isRedstone()) {
			if (logic.isSideOpen(facing)) {
				EnumFacing real = gateToReal(facing);
				World w = getWorld();
				BlockPos p = getPos().offset(real);
				Predicate<TileEntity> predicate = tileEntity -> true;

				int mpValue = RedstoneUtils.getModdedWeakPower(w, p, real, getSide(), predicate);
				if (mpValue >= 0) {
					values[i] = (byte) mpValue;
				} else {
					TileEntity tile = w.getTileEntity(p);
					if (tile != null && tile.hasCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite())) {
						// TODO: FIXME - this is a hack
						if (!(tile instanceof TileWire) || ((TileWire) tile).getWire().getLocation().facing == getOrientation().facing) {
							values[i] = (byte) tile.getCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite()).getRedstoneSignal();
						}
					} else {
						IBlockState s = w.getBlockState(p);
						if (RedstoneUtils.canConnectFace(w, p, s, real, getSide())) {
							if (s.getBlock() instanceof BlockRedstoneWire) {
								values[i] = s.getValue(BlockRedstoneWire.POWER).byteValue();
							} else {
								values[i] = (byte) s.getWeakPower(w, p, real);
							}
						}
					}
				}


				if (conn.isComparator() && values[i] < 15) {
					IBlockState s = w.getBlockState(p);
					if (s.hasComparatorInputOverride()) {
						values[i] = (byte) Math.max(MathHelper.clamp(s.getComparatorInputOverride(w, p), 0, 15), values[i]);
					} else if (s.isNormalCube()) {
						values[i] = 0;
						BlockPos pp = p.offset(real);
						s = w.getBlockState(pp);
						if (s.hasComparatorInputOverride()) {
							values[i] = (byte) Math.max(MathHelper.clamp(s.getComparatorInputOverride(w, pp), 0, 15), values[i]);
						} else if (!s.isNormalCube()) {
							List<EntityItemFrame> frames = world.getEntitiesWithinAABB(EntityItemFrame.class, new AxisAlignedBB(pp), (frame) -> frame != null && frame.getHorizontalFacing() == facing);
							for (EntityItemFrame frame : frames) {
								values[i] = (byte) Math.max(MathHelper.clamp(frame.getAnalogOutput(), 0, 15), values[i]);
							}
						}
					}
				}

				if (conn.isDigital()) {
					values[i] = values[i] != 0 ? (byte) 15 : 0;
				}

				if (logic.isSideInverted(facing)) {
					values[i] = values[i] != 0 ? 0 : (byte) 15;
				}
			}
		}

		if (values[i] != oldValue) {
			return true;
		}

		return false;
	}

	@Override
	public Notice createNotice(NotificationComponent component) {
		return new Notice(this, component);
	}

	@Override
	public void markGateChanged() {
		if (world.isRemote) {
			markBlockForRenderUpdate();
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
		logic.onChanged(this);
	}

	public void scheduleTick() {
		scheduleTick(2);
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
		if (pendingTick == 0) {
			pendingTick = duration;
		}
	}

	@Override
	public void validate() {
		super.validate();
		pendingTick = 1;
		pendingChange = true;
	}

	public void onNeighborBlockChange(@Nullable Block block) {
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
	private EnumFacing getClosestFace(Vec3d vec, boolean allowNulls) {
		int closestFace = -1;
		double distance = Double.MAX_VALUE;
		for (int i = 0; i <= (allowNulls ? 4 : 3); i++) {
			double d = HIT_VECTORS[i].squareDistanceTo(vec);
			if (d < distance) {
				closestFace = i;
				distance = d;
			}
		}

		if (closestFace >= 0 && closestFace < 4) {
			return EnumFacing.byIndex(closestFace + 2);
		} else {
			return null;
		}
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
					EnumFacing closestFace = getClosestFace(vec, true);

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
			if (!(changed = logic.onRightClick(this, playerIn, vec, hand))) {
				EnumFacing closestFace = getClosestFace(vec, true);

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
			}
		}

		if (changed) {
			if (!remote) {
				onChanged();
				pendingChange = true;
				markBlockForUpdate();
			} else {
				markBlockForRenderUpdate();
			}
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
			orientation = orientation.pointTopTo(gateToReal(getClosestFace(vec, false)));
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
		if (!isClient && pendingTick != 0) {
			tag.setByte("p", (byte) pendingTick);
			tag.setBoolean("pch", pendingChange);
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
		if (tag.hasKey("p")) {
			pendingTick = tag.getByte("p");
			pendingChange = tag.getBoolean("pch");
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
	public boolean hasFastRenderer() {
		return true;
	}
}
