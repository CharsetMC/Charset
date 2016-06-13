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

import mcmultipart.microblock.IMicroblock;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.api.wires.WireFace;

public final class WireUtils {
	private static final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][]{
			{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
			{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH},
			{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
	};

	private WireUtils() {

	}

	public static EnumFacing[] getConnectionsForRender(WireFace face) {
		return CONNECTION_DIRS[face.ordinal()];
	}

	private static boolean isBlockingPart(IMultipartContainer container, PartSlot slot) {
		ISlottedPart part = container.getPartInSlot(slot);
		if (part instanceof IMicroblock.IFaceMicroblock) {
			return !((IMicroblock.IFaceMicroblock) part).isEdgeHollow();
		} else {
			return part != null;
		}
	}

	public static boolean canConnectInternal(PartWire wire, WireFace side) {
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

		PartWire wire2 = getWire(container, side);
		return wire2 != null && wire2.calculateConnectionWire(wire);
	}

	public static boolean canConnectExternal(PartWire wire, EnumFacing facing) {
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
			PartWire wire2 = getWire(container2, wire.location);
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

				return wire2.calculateConnectionWire(wire);
			}
		}

		return wire.calculateConnectionNonWire(wire.getPos().offset(facing), facing.getOpposite());
	}

	public static boolean canConnectCorner(PartWire wire, EnumFacing direction) {
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

		PartWire wire2 = getWire(container, WireFace.get(direction.getOpposite()));

		if (wire2 == null || wire2.isCornerOccluded(side.getOpposite()) || !wire2.calculateConnectionWire(wire)) {
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

	public static PartWire getWire(IMultipartContainer container, WireFace face) {
		if (container != null) {
			ISlottedPart part = container.getPartInSlot(getSlotForFace(face));
			return part instanceof PartWire ? (PartWire) part : null;
		} else {
			return null;
		}
	}
}
