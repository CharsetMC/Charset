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

package pl.asie.simplelogic.wires;

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

import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.utils.redstone.RedstoneUtils;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireUtils;
import pl.asie.simplelogic.wires.logic.PartWireSignalBase;

public final class LogicWireUtils {
	private static final Set<Block> WIRE_PLACEABLE = new HashSet<Block>();

	static {
		WIRE_PLACEABLE.add(Blocks.GLOWSTONE);
		WIRE_PLACEABLE.add(Blocks.PISTON);
		WIRE_PLACEABLE.add(Blocks.PISTON_EXTENSION);
		WIRE_PLACEABLE.add(Blocks.STICKY_PISTON);
	}

	private LogicWireUtils() {

	}

	public static int getRedstoneWireLevel(IBlockAccess world, BlockPos pos, WireFace face) {
		Wire wire = WireUtils.getWire(world, pos, face);
		return wire instanceof PartWireSignalBase ? ((PartWireSignalBase) wire).getSignalLevel() : 0;
	}

	public static int getInsulatedWireLevel(IBlockAccess world, BlockPos pos, WireFace face, int color) {
		Wire wire = WireUtils.getWire(world, pos, face);
		if (wire instanceof PartWireSignalBase) {
			PartWireSignalBase signalWire = (PartWireSignalBase) wire;
			switch (signalWire.getWireType()) {
				case NORMAL:
					return signalWire.getSignalLevel();
				case INSULATED:
					return signalWire.getColor() == color ? signalWire.getSignalLevel() : 0;
				case BUNDLED:
					return signalWire.getBundledSignalLevel(color);
			}
		}

		return 0;
	}

	public static int getBundledWireLevel(IBlockAccess world, BlockPos pos, WireFace face, int color) {
		Wire wire = WireUtils.getWire(world, pos, face);
		if (wire instanceof PartWireSignalBase) {
			PartWireSignalBase signalWire = (PartWireSignalBase) wire;
			switch (signalWire.getWireType()) {
				case INSULATED:
					return signalWire.getColor() == color ? signalWire.getSignalLevel() : 0;
				case BUNDLED:
					return signalWire.getBundledSignalLevel(color);
			}
		}

		return 0;
	}

	// IGNORES WIRES.
	public static int getStrongRedstoneLevel(Wire wire, BlockPos pos, IBlockState state, EnumFacing facing, WireFace face) {
		if (facing == null) {
			return 0;
		}

		// If the block has a wire on it, ignore the strong redstone level altogether.
		if (WireUtils.hasCapability(wire, pos, Capabilities.REDSTONE_EMITTER, facing.getOpposite(), false)) {
			return 0;
		}

		World world = wire.getContainer().world();
		return state.getStrongPower(world, pos, facing);
	}

	// IGNORES WIRES.
	public static int getWeakRedstoneLevel(Wire wire, BlockPos pos, IBlockState state, EnumFacing facing, WireFace face) {
		World world = wire.getContainer().world();

		// Step 1: Check with mods.
		int power = RedstoneUtils.getModdedWeakPower(world, pos, facing, face.facing);
		if (power >= 0) {
			return power;
		}

		// Step 2: Check IRedstoneEmitter.
		EnumFacing facingOpposite = facing == null ? null : facing.getOpposite();
		if (WireUtils.hasCapability(wire, pos, Capabilities.REDSTONE_EMITTER, facingOpposite, false)) {
			IRedstoneEmitter emitter = WireUtils.getCapability(wire, pos, Capabilities.REDSTONE_EMITTER, facingOpposite, true);
			if (emitter instanceof PartWireSignalBase) {
				return 0;
			} else {
				return emitter.getRedstoneSignal();
			}
		}

		// Step 3: Check vanilla.
		Block block = state.getBlock();

		if (block instanceof BlockRedstoneWire && face == WireFace.DOWN) {
			return state.getValue(BlockRedstoneWire.POWER);
		}

		return block.shouldCheckWeakPower(state, world, pos, facing)
				? state.getStrongPower(world, pos, facing)
				: state.getWeakPower(world, pos, facing);
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
