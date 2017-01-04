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

package pl.asie.charset.lib.utils;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;

public final class RotationUtils {
	private RotationUtils() {

	}

	public static boolean rotateAround(World world, BlockPos pos, EnumFacing axis) {
		return rotateAround(world, pos, axis, 1);
	}

	public static boolean rotateAround(World world, BlockPos pos, EnumFacing axis, int count) {
		count = count & 3;
		if (count == 3) {
			count = 1;
			axis = axis.getOpposite();
		}

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof IAxisRotatable.IAxisRotatableBlock) {
			for (int i = 0; i < count; i++)
				if (!((IAxisRotatable.IAxisRotatableBlock) state.getBlock()).rotateAround(world, pos, axis))
					return false;

			return true;
		}

		if (state.getBlock().hasTileEntity(state)) {
			TileEntity tile = world.getTileEntity(pos);
			IAxisRotatable rotatable = CapabilityHelper.get(Capabilities.AXIS_ROTATABLE, tile, axis);
			if (rotatable != null) {
				for (int i = 0; i < count; i++)
					if (!rotatable.rotateAround(axis))
						return false;

				return true;
			}
		}

		if (axis.getAxis() == EnumFacing.Axis.Y) {
			for (IProperty<?> prop : state.getProperties().keySet()) {
				if (prop.getName().equals("facing") || prop.getName().equals("rotation")) {
					EnumFacing facing = (EnumFacing) state.getValue(prop);
					for (int i = 0; i < (axis == EnumFacing.UP ? (4 - count) : count); i++) {
						facing = facing.rotateAround(EnumFacing.Axis.Y);
					}

					if (prop.getAllowedValues().contains(facing)) {
						world.setBlockState(pos, state.withProperty((IProperty<EnumFacing>) prop, facing));
						return true;
					}
				}
			}
		}

		return false;
	}

	public static Vec3d rotateVec(Vec3d vec, EnumFacing facing) {
		switch (facing) {
			case DOWN:
			default:
				return vec;
			case UP:
				return new Vec3d(vec.xCoord, 1 - vec.yCoord, vec.zCoord);
			case NORTH:
				return new Vec3d(vec.xCoord, vec.zCoord, vec.yCoord);
			case SOUTH:
				return new Vec3d(vec.xCoord, vec.zCoord, 1 - vec.yCoord);
			case WEST:
				return new Vec3d(vec.yCoord, vec.zCoord, vec.xCoord);
			case EAST:
				return new Vec3d(1 - vec.yCoord, vec.zCoord, vec.xCoord);
		}
	}

	public static AxisAlignedBB rotateFace(AxisAlignedBB box, EnumFacing facing) {
		switch (facing) {
			case DOWN:
			default:
				return box;
			case UP:
				return new AxisAlignedBB(box.minX, 1 - box.maxY, box.minZ, box.maxX, 1 - box.minY, box.maxZ);
			case NORTH:
				return new AxisAlignedBB(box.minX, box.minZ, box.minY, box.maxX, box.maxZ, box.maxY);
			case SOUTH:
				return new AxisAlignedBB(box.minX, box.minZ, 1 - box.maxY, box.maxX, box.maxZ, 1 - box.minY);
			case WEST:
				return new AxisAlignedBB(box.minY, box.minZ, box.minX, box.maxY, box.maxZ, box.maxX);
			case EAST:
				return new AxisAlignedBB(1 - box.maxY, box.minZ, box.minX, 1 - box.minY, box.maxZ, box.maxX);
		}
	}
}
