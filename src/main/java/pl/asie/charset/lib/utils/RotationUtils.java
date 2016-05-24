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

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public final class RotationUtils {
	private RotationUtils() {

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
