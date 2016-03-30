package pl.asie.charset.lib.utils;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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

	public static Result getCollision(World world, BlockPos pos, EntityPlayer player, List<AxisAlignedBB> list) {
		double reachDistance = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).interactionManager.getBlockReachDistance() : 5.0d;

		Vec3d lookVec = player.getLookVec();
		Vec3d origin = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3d direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

		return getCollision(world, pos, origin, direction, list);
	}

	public static RayTraceResult getCollision(World world, BlockPos pos, EntityPlayer player, AxisAlignedBB aabb, int subHit) {
		double reachDistance = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).interactionManager.getBlockReachDistance() : 5.0d;

		Vec3d lookVec = player.getLookVec();
		Vec3d origin = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3d direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

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

		Vec3d vec0 = start.getIntermediateWithXValue(end, aabb.minX);
		Vec3d vec1 = start.getIntermediateWithXValue(end, aabb.maxX);
		Vec3d vec2 = start.getIntermediateWithYValue(end, aabb.minY);
		Vec3d vec3 = start.getIntermediateWithYValue(end, aabb.maxY);
		Vec3d vec4 = start.getIntermediateWithZValue(end, aabb.minZ);
		Vec3d vec5 = start.getIntermediateWithZValue(end, aabb.maxZ);

		if (!isVecInsideYZBounds(aabb, vec0)) {
			vec0 = null;
		}

		if (!isVecInsideYZBounds(aabb, vec1)) {
			vec1 = null;
		}

		if (!isVecInsideXZBounds(aabb, vec2)) {
			vec2 = null;
		}

		if (!isVecInsideXZBounds(aabb, vec3)) {
			vec3 = null;
		}

		if (!isVecInsideXYBounds(aabb, vec4)) {
			vec4 = null;
		}

		if (!isVecInsideXYBounds(aabb, vec5)) {
			vec5 = null;
		}

		Vec3d vec6 = null;

		if (vec0 != null && (vec6 == null || start.squareDistanceTo(vec0) < start.squareDistanceTo(vec6))) {
			vec6 = vec0;
		}

		if (vec1 != null && (vec6 == null || start.squareDistanceTo(vec1) < start.squareDistanceTo(vec6))) {
			vec6 = vec1;
		}

		if (vec2 != null && (vec6 == null || start.squareDistanceTo(vec2) < start.squareDistanceTo(vec6))) {
			vec6 = vec2;
		}

		if (vec3 != null && (vec6 == null || start.squareDistanceTo(vec3) < start.squareDistanceTo(vec6))) {
			vec6 = vec3;
		}

		if (vec4 != null && (vec6 == null || start.squareDistanceTo(vec4) < start.squareDistanceTo(vec6))) {
			vec6 = vec4;
		}

		if (vec5 != null && (vec6 == null || start.squareDistanceTo(vec5) < start.squareDistanceTo(vec6))) {
			vec6 = vec5;
		}

		if (vec6 == null) {
			return null;
		} else {
			EnumFacing enumfacing = null;

			if (vec6 == vec0) {
				enumfacing = EnumFacing.WEST;
			}

			if (vec6 == vec1) {
				enumfacing = EnumFacing.EAST;
			}

			if (vec6 == vec2) {
				enumfacing = EnumFacing.DOWN;
			}

			if (vec6 == vec3) {
				enumfacing = EnumFacing.UP;
			}

			if (vec6 == vec4) {
				enumfacing = EnumFacing.NORTH;
			}

			if (vec6 == vec5) {
				enumfacing = EnumFacing.SOUTH;
			}

			RayTraceResult mop = new RayTraceResult(vec6.addVector((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), enumfacing, pos);
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
