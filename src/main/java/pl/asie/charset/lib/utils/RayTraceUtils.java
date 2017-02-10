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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public final class RayTraceUtils {
	public static class Result {
		public final AxisAlignedBB box;
		public final RayTraceResult hit;

		public Result(RayTraceResult mop, AxisAlignedBB box) {
			this.hit = mop;
			this.box = box;
		}

		public boolean valid() {
			return hit != null && box != null;
		}
	}

	private RayTraceUtils() {

	}

	public static Vec3d getStart(EntityLivingBase player) {
		return new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
	}

	public static Vec3d getEnd(EntityLivingBase player) {
		double reachDistance = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).interactionManager.getBlockReachDistance() : 5.0d;
		Vec3d lookVec = player.getLookVec();

		return getStart(player).addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);
	}

	public static Result getCollision(World world, BlockPos pos, EntityLivingBase player, List<AxisAlignedBB> list) {
		Vec3d origin = getStart(player);
		Vec3d direction = getEnd(player);

		return getCollision(world, pos, origin, direction, list);
	}

	public static RayTraceResult getCollision(World world, BlockPos pos, EntityLivingBase player, AxisAlignedBB aabb, int subHit) {
		Vec3d origin = getStart(player);
		Vec3d direction = getEnd(player);

		return getCollision(pos, origin, direction, aabb, subHit);
	}

	public static Result getCollision(World world, BlockPos pos, Vec3d origin, Vec3d direction, List<AxisAlignedBB> list) {
		double minDistance = Double.POSITIVE_INFINITY;
		RayTraceResult hit = null;

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == null) {
				continue;
			}

			RayTraceResult mop = getCollision(pos, origin, direction, list.get(i), i);
			if (mop != null) {
				double d = mop.hitVec.squareDistanceTo(origin);
				if (d < minDistance) {
					minDistance = d;
					hit = mop;
				}
			}
		}

		return new Result(hit, hit != null ? list.get(hit.subHit) : null);
	}

	public static RayTraceResult getCollision(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB aabb, int subHit) {
		start = start.addVector((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()));
		end = end.addVector((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()));

		Vec3d vecWest = start.getIntermediateWithXValue(end, aabb.minX);
		Vec3d vecEast = start.getIntermediateWithXValue(end, aabb.maxX);
		Vec3d vecDown = start.getIntermediateWithYValue(end, aabb.minY);
		Vec3d vecUp = start.getIntermediateWithYValue(end, aabb.maxY);
		Vec3d vecNorth = start.getIntermediateWithZValue(end, aabb.minZ);
		Vec3d vecSouth = start.getIntermediateWithZValue(end, aabb.maxZ);

		if (!isVecInsideYZBounds(aabb, vecWest)) {
			vecWest = null;
		}

		if (!isVecInsideYZBounds(aabb, vecEast)) {
			vecEast = null;
		}

		if (!isVecInsideXZBounds(aabb, vecDown)) {
			vecDown = null;
		}

		if (!isVecInsideXZBounds(aabb, vecUp)) {
			vecUp = null;
		}

		if (!isVecInsideXYBounds(aabb, vecNorth)) {
			vecNorth = null;
		}

		if (!isVecInsideXYBounds(aabb, vecSouth)) {
			vecSouth = null;
		}

		Vec3d vecHit = null;

		if (vecWest != null && (vecHit == null || start.squareDistanceTo(vecWest) < start.squareDistanceTo(vecHit))) {
			vecHit = vecWest;
		}

		if (vecEast != null && (vecHit == null || start.squareDistanceTo(vecEast) < start.squareDistanceTo(vecHit))) {
			vecHit = vecEast;
		}

		if (vecDown != null && (vecHit == null || start.squareDistanceTo(vecDown) < start.squareDistanceTo(vecHit))) {
			vecHit = vecDown;
		}

		if (vecUp != null && (vecHit == null || start.squareDistanceTo(vecUp) < start.squareDistanceTo(vecHit))) {
			vecHit = vecUp;
		}

		if (vecNorth != null && (vecHit == null || start.squareDistanceTo(vecNorth) < start.squareDistanceTo(vecHit))) {
			vecHit = vecNorth;
		}

		if (vecSouth != null && (vecHit == null || start.squareDistanceTo(vecSouth) < start.squareDistanceTo(vecHit))) {
			vecHit = vecSouth;
		}

		if (vecHit == null) {
			return null;
		} else {
			EnumFacing sideHit = null;

			if (vecHit == vecWest) {
				sideHit = EnumFacing.WEST;
			}

			if (vecHit == vecEast) {
				sideHit = EnumFacing.EAST;
			}

			if (vecHit == vecDown) {
				sideHit = EnumFacing.DOWN;
			}

			if (vecHit == vecUp) {
				sideHit = EnumFacing.UP;
			}

			if (vecHit == vecNorth) {
				sideHit = EnumFacing.NORTH;
			}

			if (vecHit == vecSouth) {
				sideHit = EnumFacing.SOUTH;
			}

			RayTraceResult mop = new RayTraceResult(vecHit.addVector((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), sideHit, pos);
			mop.subHit = subHit;
			return mop;
		}
	}

	private static boolean isVecInsideYZBounds(AxisAlignedBB aabb, Vec3d point) {
		return point == null ? false : point.yCoord >= aabb.minY && point.yCoord <= aabb.maxY && point.zCoord >= aabb.minZ && point.zCoord <= aabb.maxZ;
	}

	private static boolean isVecInsideXZBounds(AxisAlignedBB aabb, Vec3d point) {
		return point == null ? false : point.xCoord >= aabb.minX && point.xCoord <= aabb.maxX && point.zCoord >= aabb.minZ && point.zCoord <= aabb.maxZ;
	}

	private static boolean isVecInsideXYBounds(AxisAlignedBB aabb, Vec3d point) {
		return point == null ? false : point.xCoord >= aabb.minX && point.xCoord <= aabb.maxX && point.yCoord >= aabb.minY && point.yCoord <= aabb.maxY;
	}
}
