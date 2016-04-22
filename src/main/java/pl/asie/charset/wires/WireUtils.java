package pl.asie.charset.wires;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import mcmultipart.microblock.IMicroblock;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.IRedstonePart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.Capabilities;
import pl.asie.charset.lib.utils.MultipartUtils;
import pl.asie.charset.lib.utils.RedstoneUtils;
import pl.asie.charset.wires.logic.PartWireBase;

public final class WireUtils {
	public static boolean WIRES_CONNECT = true;
	private static final Set<Block> WIRE_PLACEABLE = new HashSet<Block>();
	private static final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][]{
			{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH},
			{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
	};

	static {
		WIRE_PLACEABLE.add(Blocks.GLOWSTONE);
		WIRE_PLACEABLE.add(Blocks.PISTON);
		WIRE_PLACEABLE.add(Blocks.PISTON_EXTENSION);
		WIRE_PLACEABLE.add(Blocks.STICKY_PISTON);
	}

	private WireUtils() {

	}

	public static EnumFacing[] getConnectionsForRender(WireFace face) {
		return CONNECTION_DIRS[face.ordinal()];
	}

	public static int getRedstoneWireLevel(IMultipartContainer container, WireFace face) {
		PartWireBase wire = getWire(container, face);
		return wire.getSignalLevel();
	}

	public static int getInsulatedWireLevel(IMultipartContainer container, WireFace face, int color) {
		PartWireBase wire = getWire(container, face);
		switch (wire.type.type()) {
			case NORMAL:
				return wire.getSignalLevel();
			case INSULATED:
				return wire.type.color() == color ? wire.getSignalLevel() : 0;
			case BUNDLED:
				return wire.getBundledSignalLevel(color);
		}

		return 0;
	}

	public static int getBundledWireLevel(IMultipartContainer container, WireFace face, int color) {
		PartWireBase wire = getWire(container, face);
		switch (wire.type.type()) {
			case INSULATED:
				return wire.type.color() == color ? wire.getSignalLevel() : 0;
			case BUNDLED:
				return wire.getBundledSignalLevel(color);
		}

		return 0;
	}

	private static boolean isBlockingPart(IMultipartContainer container, PartSlot slot) {
		ISlottedPart part = container.getPartInSlot(slot);
		if (part instanceof IMicroblock.IFaceMicroblock) {
			return !((IMicroblock.IFaceMicroblock) part).isEdgeHollow();
		} else {
			return part != null;
		}
	}

	public static boolean canConnectInternal(PartWireBase wire, WireFace side) {
		WireFace location = wire.location;
		IMultipartContainer container = wire.getContainer();

		if (side == location) {
			return false;
		}

		if (side != WireFace.CENTER && location != WireFace.CENTER) {
			if (isBlockingPart(container, PartSlot.getEdgeSlot(side.facing, location.facing))) {
				return false;
			}
		}

		PartWireBase wire2 = getWire(container, side);
		return wire2 != null && wire2.type.connects(wire.type);
	}

	public static boolean canConnectExternal(PartWireBase wire, EnumFacing facing) {
		IMultipartContainer container = wire.getContainer();

		if (isBlockingPart(container, PartSlot.getFaceSlot(facing))) {
			return false;
		}

		if (wire.location != WireFace.CENTER && isBlockingPart(container, PartSlot.getEdgeSlot(facing, wire.location.facing))) {
			return false;
		}

		BlockPos pos2 = wire.getPos().offset(facing);
		IMultipartContainer container2 = MultipartHelper.getPartContainer(wire.getWorld(), pos2);

		if (container2 != null) {
			PartWireBase wire2 = getWire(container2, wire.location);
			if (wire2 != null) {
				if (isBlockingPart(container2, PartSlot.getFaceSlot(facing.getOpposite()))) {
					return false;
				}

				if (wire2.isOccluded(facing.getOpposite())) {
					return false;
				}

				if (wire.location != WireFace.CENTER && isBlockingPart(container2, PartSlot.getEdgeSlot(facing.getOpposite(), wire.location.facing))) {
					return false;
				}

				return wire2.type.connects(wire.type);
			}
		}

		if (wire.type.type() == WireType.BUNDLED) {
			if (MultipartUtils.hasCapability(Capabilities.BUNDLED_EMITTER, wire.getWorld(), pos2, WireUtils.getSlotForFace(wire.location), facing.getOpposite())) {
				return true;
			}

			if (MultipartUtils.hasCapability(Capabilities.BUNDLED_RECEIVER, wire.getWorld(), pos2, WireUtils.getSlotForFace(wire.location), facing.getOpposite())) {
				return true;
			}
		} else {
			if (MultipartUtils.hasCapability(Capabilities.REDSTONE_EMITTER, wire.getWorld(), pos2, WireUtils.getSlotForFace(wire.location), facing.getOpposite())) {
				return true;
			}

			if (MultipartUtils.hasCapability(Capabilities.REDSTONE_RECEIVER, wire.getWorld(), pos2, WireUtils.getSlotForFace(wire.location), facing.getOpposite())) {
				return true;
			}

			IBlockState connectingState = wire.getWorld().getBlockState(pos2);
			Block connectingBlock = connectingState.getBlock();

			if (wire.location == WireFace.CENTER && !connectingBlock.isSideSolid(connectingState, wire.getWorld(), pos2, facing.getOpposite())) {
				return false;
			}

			WIRES_CONNECT = false;

			if (RedstoneUtils.canConnectFace(wire.getWorld(), pos2, connectingState, facing, wire.location.facing)) {
				WIRES_CONNECT = true;
				return true;
			}

			WIRES_CONNECT = true;
		}

		return false;
	}

	public static boolean canConnectCorner(PartWireBase wire, EnumFacing direction) {
		if (wire.location == WireFace.CENTER || wire.isCornerOccluded(direction)) {
			return false;
		}

		EnumFacing side = wire.location.facing;
		IMultipartContainer container = wire.getContainer();

		if (isBlockingPart(container, PartSlot.getFaceSlot(direction))
				|| isBlockingPart(container, PartSlot.getEdgeSlot(direction, wire.location.facing))) {
			return false;
		}

		BlockPos middlePos = wire.getPos().offset(direction);
		if (wire.getWorld().isSideSolid(middlePos, direction.getOpposite()) || wire.getWorld().isSideSolid(middlePos, side.getOpposite())) {
			return false;
		}

		BlockPos cornerPos = middlePos.offset(side);
		container = MultipartHelper.getPartContainer(wire.getWorld(), cornerPos);
		if (container == null) {
			return false;
		}

		if (isBlockingPart(container, PartSlot.getFaceSlot(side.getOpposite()))
				|| isBlockingPart(container, PartSlot.getEdgeSlot(side.getOpposite(), direction.getOpposite()))) {
			return false;
		}

		PartWireBase wire2 = getWire(container, WireFace.get(direction.getOpposite()));

		if (wire2 == null || wire2.isCornerOccluded(side.getOpposite()) || !wire2.type.connects(wire.type)) {
			return false;
		}

		container = MultipartHelper.getPartContainer(wire.getWorld(), middlePos);
		if (container != null) {
			if (isBlockingPart(container, PartSlot.getFaceSlot(direction.getOpposite()))
					|| isBlockingPart(container, PartSlot.getFaceSlot(side))
					|| isBlockingPart(container, PartSlot.getEdgeSlot(direction.getOpposite(), side))) {
				return false;
			}
		}

		return true;
	}

	public static PartSlot getSlotForFace(WireFace face) {
		return PartSlot.VALUES[face.ordinal()];
	}

	public static PartWireBase getWire(IMultipartContainer container, WireFace face) {
		if (container != null) {
			ISlottedPart part = container.getPartInSlot(getSlotForFace(face));
			return part instanceof PartWireBase ? (PartWireBase) part : null;
		} else {
			return null;
		}
	}

	public static int getRedstoneLevel(World world, BlockPos pos, IBlockState state, EnumFacing facing, WireFace face, boolean weak) {
		int power = 0;

		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container != null) {
			if (getWire(container, face) != null || getWire(container, WireFace.get(facing.getOpposite())) != null) {
				return 0;
			}

			for (IMultipart part : container.getParts()) {
				if (!(part instanceof PartWireBase)) {
					if (part instanceof IRedstonePart) {
						power = Math.max(power, ((IRedstonePart) part).getWeakSignal(facing.getOpposite()));
					}
				}
			}
		}

		if (MultipartUtils.hasCapability(Capabilities.REDSTONE_EMITTER, world, pos, WireUtils.getSlotForFace(face), facing)) {
			power = Math.max(power, MultipartUtils.getCapability(Capabilities.REDSTONE_EMITTER, world, pos, WireUtils.getSlotForFace(face), facing).getRedstoneSignal());
		}

		Block block = state.getBlock();

		if (power == 0) {
			if (weak) {
				if (block instanceof BlockRedstoneWire && face == WireFace.DOWN) {
					return state.getValue(BlockRedstoneWire.POWER);
				}

				return block.shouldCheckWeakPower(state, world, pos, facing)
						? block.getStrongPower(state, world, pos, facing)
						: block.getWeakPower(state, world, pos, facing);
			} else {
				return block.getStrongPower(state, world, pos, facing);
			}
		} else {
			return power;
		}
	}

	public static int width(WireType type) {
		switch (type) {
			case NORMAL:
				return 2;
			case INSULATED:
				return 4;
			case BUNDLED:
				return 6;
		}

		return 0;
	}

	public static int height(WireType type) {
		switch (type) {
			case NORMAL:
				return 2;
			case INSULATED:
				return 3;
			case BUNDLED:
				return 4;
		}

		return 0;
	}

	public static float getWireHitboxWidth(PartWireBase wire) {
		return wire.type.width() / 16.0f;
	}

	public static float getWireHitboxHeight(PartWireBase wire) {
		return wire.type.height() / 16.0f;
	}

	public static boolean canPlaceWire(World world, BlockPos pos, EnumFacing side) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (WIRE_PLACEABLE.contains(state.getBlock())) {
			return true;
		}

		return block.isSideSolid(state, world, pos, side);
	}
}
