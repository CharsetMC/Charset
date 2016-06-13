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

package pl.asie.charset.wires;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
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
import pl.asie.charset.wires.logic.PartWireSignalBase;

public final class WireUtils {
	private static final Set<Block> WIRE_PLACEABLE = new HashSet<Block>();

	static {
		WIRE_PLACEABLE.add(Blocks.GLOWSTONE);
		WIRE_PLACEABLE.add(Blocks.PISTON);
		WIRE_PLACEABLE.add(Blocks.PISTON_EXTENSION);
		WIRE_PLACEABLE.add(Blocks.STICKY_PISTON);
	}

	private WireUtils() {

	}

	public static int getRedstoneWireLevel(IMultipartContainer container, WireFace face) {
		PartWireSignalBase wire = getWire(container, face);
		return wire.getSignalLevel();
	}

	public static int getInsulatedWireLevel(IMultipartContainer container, WireFace face, int color) {
		PartWireSignalBase wire = getWire(container, face);
		switch (wire.getWireType()) {
			case NORMAL:
				return wire.getSignalLevel();
			case INSULATED:
				return wire.getColor() == color ? wire.getSignalLevel() : 0;
			case BUNDLED:
				return wire.getBundledSignalLevel(color);
		}

		return 0;
	}

	public static int getBundledWireLevel(IMultipartContainer container, WireFace face, int color) {
		PartWireSignalBase wire = getWire(container, face);
		switch (wire.getWireType()) {
			case INSULATED:
				return wire.getColor() == color ? wire.getSignalLevel() : 0;
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

	public static PartSlot getSlotForFace(WireFace face) {
		return PartSlot.VALUES[face.ordinal()];
	}

	public static PartWireSignalBase getWire(IMultipartContainer container, WireFace face) {
		if (container != null) {
			ISlottedPart part = container.getPartInSlot(getSlotForFace(face));
			return part instanceof PartWireSignalBase ? (PartWireSignalBase) part : null;
		} else {
			return null;
		}
	}

	public static int getRedstoneLevel(World world, BlockPos pos, IBlockState state, EnumFacing facing, WireFace face, boolean weak) {
		EnumFacing facingOpposite = facing == null ? null : facing.getOpposite();
		int power = 0;

		IMultipartContainer container = MultipartHelper.getPartContainer(world, pos);
		if (container != null) {
			if (getWire(container, face) != null || getWire(container, WireFace.get(facingOpposite)) != null) {
				return 0;
			}

			for (IMultipart part : container.getParts()) {
				if (!(part instanceof PartWireSignalBase)) {
					if (part instanceof IRedstonePart) {
						power = Math.max(power, ((IRedstonePart) part).getWeakSignal(facingOpposite));
					}
				}
			}
		}

		if (MultipartUtils.hasCapability(Capabilities.REDSTONE_EMITTER, world, pos, WireUtils.getSlotForFace(face), facingOpposite)) {
			power = Math.max(power, MultipartUtils.getCapability(Capabilities.REDSTONE_EMITTER, world, pos, WireUtils.getSlotForFace(face), facingOpposite).getRedstoneSignal());
		}

		Block block = state.getBlock();

		if (power == 0) {
			if (weak) {
				if (block instanceof BlockRedstoneWire && face == WireFace.DOWN) {
					return state.getValue(BlockRedstoneWire.POWER);
				}

				return block.shouldCheckWeakPower(state, world, pos, facing)
						? state.getStrongPower(world, pos, facing)
						: state.getWeakPower(world, pos, facing);
			} else {
				return state.getStrongPower(world, pos, facing);
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

	public static boolean canPlaceWire(IBlockAccess world, BlockPos pos, EnumFacing side) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (WIRE_PLACEABLE.contains(state.getBlock())) {
			return true;
		}

		return block.isSideSolid(state, world, pos, side);
	}
}
