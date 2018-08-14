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

package pl.asie.charset.lib.utils.redstone;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.multipart.MultipartRedstoneHelper;
import mcmultipart.api.slot.EnumEdgeSlot;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by asie on 1/6/16.
 */
public final class RedstoneUtils {
	private RedstoneUtils() {

	}

	private static final List<IRedstoneGetter> GETTERS = new ArrayList<>();

	public static int getComparatorValue(IItemHandler handler, int max) {
		float value = 0;
		int count = 0;

		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				value += stack.getCount() / Math.min(stack.getMaxStackSize(), handler.getSlotLimit(i));
				count++;
			}
		}

		if (count > 0) {
			return 1 + MathHelper.floor(value * (max - 1) / (float) handler.getSlots());
		} else {
			return 0;
		}
	}

	public static void addRedstoneGetter(IRedstoneGetter getter) {
		GETTERS.add(getter);
	}

	public static int getModdedWeakPower(IBlockAccess w, BlockPos p, EnumFacing face, EnumFacing edge, Predicate<TileEntity> tileEntityPredicate) {
		for (IRedstoneGetter getter : GETTERS) {
			int v = getter.get(w, p, face, edge, tileEntityPredicate);
			if (v >= 0) {
				return v;
			}
		}

		return -1;
	}

	public static byte[] getModdedBundledPower(IBlockAccess w, BlockPos p, EnumFacing face, EnumFacing edge, Predicate<TileEntity> tileEntityPredicate) {
		for (IRedstoneGetter getter : GETTERS) {
			byte[] v = getter.getBundled(w, p, face, edge, tileEntityPredicate);
			if (v != null) {
				return v;
			}
		}

		return null;
	}

	// TODO: Evaluate me
	public static int getRedstonePower(World world, BlockPos pos, EnumFacing facing) {
		IBlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if (block instanceof BlockRedstoneWire) {
			return iblockstate.getValue(BlockRedstoneWire.POWER);
		}

		return block.shouldCheckWeakPower(iblockstate, world, pos, facing) ? world.getStrongPower(pos) : iblockstate.getWeakPower(world, pos, facing);
	}

	public static boolean canConnectFace(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side, EnumFacing face) {
		Block block = state.getBlock();
		if ((block instanceof BlockRedstoneDiode || block instanceof BlockRedstoneWire || block instanceof BlockDaylightDetector || block instanceof BlockBasePressurePlate) && face != EnumFacing.DOWN) {
			return false;
		}

		if (block instanceof BlockLever && face != state.getValue(BlockLever.FACING).getFacing().getOpposite()) {
			return false;
		}

		if (block instanceof BlockButton && face != state.getValue(BlockButton.FACING).getOpposite()) {
			return false;
		}

		if (Loader.isModLoaded("mcmultipart")) {
			return canConnectRedstoneMultipart(state, block, world, pos, side, face);
		} else {
			return block.canConnectRedstone(state, world, pos, side);
		}
	}

	@net.minecraftforge.fml.common.Optional.Method(modid = "mcmultipart")
	private static boolean canConnectRedstoneMultipart(IBlockState state, Block block, IBlockAccess world, BlockPos pos, EnumFacing side, EnumFacing face) {
		Optional<IMultipartContainer> ui = MultipartHelper.getContainer(world, pos);
		if (ui.isPresent()) {
			return MCMPUtils.streamParts(ui.get(), face, side.getOpposite()).anyMatch((info) -> info.getPart().canConnectRedstone(info.getPartWorld(), info.getPartPos(), info, side));
		} else {
			return block.canConnectRedstone(state, world, pos, side);
		}
	}
}
