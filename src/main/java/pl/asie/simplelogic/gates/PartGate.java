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

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.common.util.Constants;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledReceiver;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneReceiver;
import pl.asie.charset.lib.utils.redstone.RedstoneUtils;
import pl.asie.simplelogic.gates.logic.GateLogic;
import pl.asie.simplelogic.gates.logic.GateLogicDummy;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.render.model.IRenderComparable;
import pl.asie.charset.lib.utils.*;

import javax.annotation.Nullable;

public class PartGate extends TileBase implements IRenderComparable<PartGate>, ITickable {
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
			GateLogic.Connection type = logic.getType(side);
			return type.isOutput() && type.isRedstone() ? logic.getOutputValueOutside(side) : 0;
		}

		@Override
		public void onRedstoneInputChange() {
			onChanged();
		}
	}

	public static final AxisAlignedBB[] BOXES = new AxisAlignedBB[6];
	private static final Vec3d[][] HIT_VECTORS = new Vec3d[6][];
	private final RedstoneCommunications[] COMMS = new RedstoneCommunications[4];

	static {
		for (int i = 0; i < 6; i++) {
			EnumFacing facing = EnumFacing.getFront(i);
			BOXES[i] = RotationUtils.rotateFace(new AxisAlignedBB(0, 0, 0, 1, 0.125, 1), facing);

			HIT_VECTORS[i] = new Vec3d[5];
			HIT_VECTORS[i][4] = RotationUtils.rotateVec(new Vec3d(0.5f, 0.125f, 0.5f), facing);

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

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing direction) {
		return hasRedstoneCapability(capability, direction) || super.hasCapability(capability, direction);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
		if (hasRedstoneCapability(capability, enumFacing)) {
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
		return ItemGate.getStack(this, true);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> stacks, IBlockState state, int fortune, boolean silkTouch) {
		stacks.add(ItemGate.getStack(this, silkTouch));
	}

	@Override
	public void update() {
		if (getWorld() != null && !getWorld().isRemote && pendingTick > 0) {
			pendingTick--;
			if (pendingTick == 0) {
				if (tick() || pendingChange) {
					propagateOutputs();
					pendingChange = false;
				}
			}
		}
	}

	protected boolean tick() {
		return logic.tick(this);
	}

	public void propagateOutputs() {
		world.notifyNeighborsRespectDebug(getPos(), getBlockType(), false);
		markBlockForUpdate();
	}

	public boolean updateInputs(byte[] values) {
		byte[] oldValues = new byte[4];

		boolean changed = false;
		System.arraycopy(values, 0, oldValues, 0, 4);

		for (int i = 0; i <= 3; i++) {
			EnumFacing facing = EnumFacing.getFront(i + 2);
			GateLogic.Connection conn = logic.getType(facing);
			values[i] = 0;

			if (conn.isInput() && conn.isRedstone()) {
				if (logic.isSideOpen(facing)) {
					EnumFacing real = gateToReal(facing);
					World w = getWorld();
					BlockPos p = getPos().offset(real);
					int mpValue = RedstoneUtils.getModdedWeakPower(w, p, real, getSide());
					if (mpValue >= 0) {
						values[i] = (byte) mpValue;
					} else {
						TileEntity tile = w.getTileEntity(p);
						if (tile != null && tile.hasCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite())) {
							values[i] = (byte) tile.getCapability(Capabilities.REDSTONE_EMITTER, real.getOpposite()).getRedstoneSignal();
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

					if (conn.isDigital()) {
						values[i] = values[i] != 0 ? (byte) 15 : 0;
					}

					if (logic.isSideInverted(facing)) {
						values[i] = values[i] != 0 ? 0 : (byte) 15;
					}
				}

				if (values[i] != oldValues[i]) {
					changed = true;
				}
			}
		}

		return changed;
	}

	public boolean getInverterState(EnumFacing facing) {
		byte value = logic.getType(facing).isInput() ? logic.getInputValueOutside(facing) : logic.getOutputValueInside(facing);
		return value == 0;
	}

	protected void onChanged() {
		logic.onChanged(this);
	}

	public void scheduleTick() {
		if (pendingTick == 0) {
			pendingTick = 2;
		}
	}

	@Override
	public void validate() {
		super.validate();
		pendingTick = 1;
		pendingChange = true;
	}

	public void onNeighborBlockChange(Block block) {
		if (!getWorld().isSideSolid(getPos().offset(getSide()), getSide().getOpposite())) {
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
		Vec3d[] compare = HIT_VECTORS[getSide().ordinal()];
		int closestFace = -1;
		double distance = Double.MAX_VALUE;
		for (int i = 0; i <= (allowNulls ? 4 : 3); i++) {
			double d = compare[i].squareDistanceTo(vec);
			if (d < distance) {
				closestFace = i;
				distance = d;
			}
		}

		if (closestFace >= 0 && closestFace < 4) {
			EnumFacing dir = EnumFacing.getFront(closestFace + 2);
			EnumFacing itop = getTop();
			while (itop != EnumFacing.NORTH) {
				dir = dir.rotateYCCW();
				itop = itop.rotateYCCW();
			}
			return dir;
		} else {
			return null;
		}
	}

	public boolean onActivated(EntityPlayer playerIn, EnumHand hand, float hitX, float hitY, float hitZ) {
		boolean changed = false;
		boolean remote = getWorld().isRemote;
		ItemStack stack = playerIn.getHeldItem(hand);
		Vec3d vec = new Vec3d(hitX, hitY, hitZ);

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
				}
			}
		} else {
			EnumFacing closestFace = getClosestFace(vec, true);

			if (closestFace != null) {
				if (logic.canInvertSide(closestFace) && logic.isSideInverted(closestFace)) {
					if (!remote) {
						logic.invertedSides &= ~(1 << (closestFace.ordinal() - 2));
						ItemUtils.spawnItemEntity(getWorld(), vec.addVector(getPos().getX(), getPos().getY(), getPos().getZ()),
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
			} else {
				changed = logic.onRightClick(this, playerIn, hand);
			}
		}

		if (changed) {
			if (!remote) {
				onChanged();
				pendingChange = true;
				markBlockForUpdate();
			}
			return true;
		} else {
			return false;
		}
	}

	public String getModelName() {
		return "base";
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
		readItemNBT(stack.getTagCompound());
		orientation = Orientation.fromDirection(SimpleLogicGates.onlyBottomFace ? EnumFacing.UP : face);
		orientation = orientation.pointTopTo(gateToReal(getClosestFace(new Vec3d(hitX, hitY, hitZ), false)));
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

	public void readItemNBT(NBTTagCompound tag) {
		if (tag.hasKey("logic", Constants.NBT.TAG_STRING)) {
			logic = ItemGate.getGateLogic(new ResourceLocation(tag.getString("logic")));
		}
		logic.readFromNBT(tag, false);
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
		if (tag.hasKey("logic", Constants.NBT.TAG_STRING)) {
			logic = ItemGate.getGateLogic(new ResourceLocation(tag.getString("logic")));
		}
		logic.readFromNBT(tag, isClient);
		if (tag.hasKey("m")) {
			mirrored = tag.getBoolean("m");
		}
		if (tag.hasKey("p")) {
			pendingTick = tag.getByte("p");
			pendingChange = tag.getBoolean("pch");
		}
		orientation = Orientation.getOrientation(tag.getByte("o"));

		if (isClient) {
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
				return EnumFacing.getFront(i + 2);
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

	protected EnumFacing realToGate(EnumFacing rdir) {
		if (rdir.getAxis() == orientation.facing.getAxis()) {
			return null;
		}

		for (int i = 0; i < 4; i++) {
			if (CONNECTION_DIRS[getSide().ordinal()][i] == rdir) {
				EnumFacing dir = EnumFacing.getFront(i + 2);
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
	public boolean hasFastRenderer() {
		return true;
	}
}
