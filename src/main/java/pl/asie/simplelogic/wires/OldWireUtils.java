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

package pl.asie.simplelogic.wires;

import java.util.HashSet;
import java.util.Set;

import com.sun.xml.internal.ws.wsdl.writer.document.Part;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireUtils;
import pl.asie.simplelogic.wires.logic.PartWireSignalBase;

public final class OldWireUtils {
	private static final Set<Block> WIRE_PLACEABLE = new HashSet<Block>();

	static {
		WIRE_PLACEABLE.add(Blocks.GLOWSTONE);
		WIRE_PLACEABLE.add(Blocks.PISTON);
		WIRE_PLACEABLE.add(Blocks.PISTON_EXTENSION);
		WIRE_PLACEABLE.add(Blocks.STICKY_PISTON);
	}

	private OldWireUtils() {

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

	public static int getRedstoneLevel(Wire wire, BlockPos pos, IBlockState state, EnumFacing facing, WireFace face, boolean weak) {
		EnumFacing facingOpposite = facing == null ? null : facing.getOpposite();
		int power = 0;
/*
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
*/
		if (WireUtils.hasCapability(wire, pos, Capabilities.REDSTONE_EMITTER, facingOpposite, false)) {
			power = Math.max(power, WireUtils.getCapability(wire, pos, Capabilities.REDSTONE_EMITTER, facingOpposite, false).getRedstoneSignal());
		}

		World world = wire.getContainer().world();
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
