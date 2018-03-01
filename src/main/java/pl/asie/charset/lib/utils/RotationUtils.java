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

package pl.asie.charset.lib.utils;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pl.asie.charset.api.lib.IAxisRotatable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;

public final class RotationUtils {
	// private static final WeakHashMap<Block, Boolean> withRotationCache = new WeakHashMap<>();

	private RotationUtils() {

	}

	public static int getClockwiseRotationCount(Rotation rotation) {
		switch (rotation) {
			case NONE:
			default:
				return 0;
			case CLOCKWISE_90:
				return 1;
			case CLOCKWISE_180:
				return 2;
			case COUNTERCLOCKWISE_90:
				return 3;
		}
	}

	private static boolean overridesWithRotation(IBlockState state) {
		// be it X->Z->X->Z or N->E->S->W, a 90-degree rotation will probably
		// give a result
		return state.withRotation(Rotation.CLOCKWISE_90) != state;

		/* if (withRotationCache.containsKey(block)) {
			return withRotationCache.get(block);
		}

		Class c = block.getClass();
		Method m = null;

		try {
			m = c.getMethod("func_185499_a", IBlockState.class, Rotation.class);
		} catch (Exception e) {
			try {
				m = c.getMethod("withRotation", IBlockState.class, Rotation.class);
			} catch (Exception ee) {

			}
		}

		if (m != null) {
			boolean value = m.getDeclaringClass() != Block.class;
			withRotationCache.put(block, value);
			return value;
		} else {
			withRotationCache.put(block, false);
			return false;
		} */
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

		IAxisRotatable rotatable = CapabilityHelper.get(world, pos, Capabilities.AXIS_ROTATABLE, axis,
				true, true, false);
		if (rotatable != null) {
			for (int i = 0; i < count; i++)
				if (!rotatable.rotateAround(axis, false))
					return false;

			return true;
		}

		if (axis.getAxis() == EnumFacing.Axis.Y) {
			boolean rotatedPreviously = false;
			int rotCount = (axis == EnumFacing.UP ? (4 - count) : count);
			Rotation rotation = Rotation.NONE;
			switch (rotCount) {
				case 1:
					rotation = Rotation.CLOCKWISE_90;
					break;
				case 2:
					rotation = Rotation.CLOCKWISE_180;
					break;
				case 3:
					rotation = Rotation.COUNTERCLOCKWISE_90;
					break;
			}

			IBlockState state = world.getBlockState(pos);
			if (overridesWithRotation(state) && rotation != Rotation.NONE) {
				world.setBlockState(pos, state.withRotation(rotation));
				rotatedPreviously = true;
			}

			// TODO: Add TileEntity.rotate

			if (!rotatedPreviously) {
				for (IProperty<?> prop : state.getProperties().keySet()) {
					if (prop.getName().equals("facing") || prop.getName().equals("rotation")) {
						Object facing = state.getValue(prop);
						if (facing instanceof EnumFacing) {
							for (int i = 0; i < rotCount; i++) {
								facing = ((EnumFacing) facing).rotateAround(EnumFacing.Axis.Y);
							}

							if (prop.getAllowedValues().contains(facing)) {
								world.setBlockState(pos, state.withProperty((IProperty<EnumFacing>) prop, (EnumFacing) facing));
								return true;
							}
						}
					}
				}
			} else {
				return true;
			}
		}

		return false;
	}

	@Deprecated
	public static Vec3d rotateVec(Vec3d vec, EnumFacing facing) {
		switch (facing) {
			case DOWN:
			default:
				return vec;
			case UP:
				return new Vec3d(vec.x, 1 - vec.y, vec.z);
			case NORTH:
				return new Vec3d(vec.x, vec.z, vec.y);
			case SOUTH:
				return new Vec3d(vec.x, vec.z, 1 - vec.y);
			case WEST:
				return new Vec3d(vec.y, vec.z, vec.x);
			case EAST:
				return new Vec3d(1 - vec.y, vec.z, vec.x);
		}
	}

	@Deprecated
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
