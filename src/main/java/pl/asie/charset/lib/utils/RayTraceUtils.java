package pl.asie.charset.lib.utils;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public final class RayTraceUtils {
	public static class Result {
		public final AxisAlignedBB box;
		public final MovingObjectPosition hit;

		public Result(MovingObjectPosition mop, AxisAlignedBB box) {
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
		double reachDistance = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance() : 5.0d;

		Vec3 lookVec = player.getLookVec();
		Vec3 origin = new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

		return getCollision(world, pos, origin, direction, list);
	}

	public static MovingObjectPosition getCollision(World world, BlockPos pos, EntityPlayer player, AxisAlignedBB aabb, int subHit) {
		double reachDistance = player instanceof EntityPlayerMP ? ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance() : 5.0d;

		Vec3 lookVec = player.getLookVec();
		Vec3 origin = new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

		return getCollision(pos, origin, direction, aabb, subHit);
	}

	public static Result getCollision(World world, BlockPos pos, Vec3 origin, Vec3 direction, List<AxisAlignedBB> list) {
		double minDistance = Double.POSITIVE_INFINITY;
		MovingObjectPosition hit = null;

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == null) {
				continue;
			}

			MovingObjectPosition mop = getCollision(pos, origin, direction, list.get(i), i);
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

	public static MovingObjectPosition getCollision(BlockPos pos, Vec3 start, Vec3 end, AxisAlignedBB aabb, int subHit) {
		start = start.addVector((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()));
		end = end.addVector((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()));

		Vec3 vec3 = start.getIntermediateWithXValue(end, aabb.minX);
		Vec3 vec31 = start.getIntermediateWithXValue(end, aabb.maxX);
		Vec3 vec32 = start.getIntermediateWithYValue(end, aabb.minY);
		Vec3 vec33 = start.getIntermediateWithYValue(end, aabb.maxY);
		Vec3 vec34 = start.getIntermediateWithZValue(end, aabb.minZ);
		Vec3 vec35 = start.getIntermediateWithZValue(end, aabb.maxZ);

		if (!isVecInsideYZBounds(aabb, vec3)) {
			vec3 = null;
		}

		if (!isVecInsideYZBounds(aabb, vec31)) {
			vec31 = null;
		}

		if (!isVecInsideXZBounds(aabb, vec32)) {
			vec32 = null;
		}

		if (!isVecInsideXZBounds(aabb, vec33)) {
			vec33 = null;
		}

		if (!isVecInsideXYBounds(aabb, vec34)) {
			vec34 = null;
		}

		if (!isVecInsideXYBounds(aabb, vec35)) {
			vec35 = null;
		}

		Vec3 vec36 = null;

		if (vec3 != null && (vec36 == null || start.squareDistanceTo(vec3) < start.squareDistanceTo(vec36))) {
			vec36 = vec3;
		}

		if (vec31 != null && (vec36 == null || start.squareDistanceTo(vec31) < start.squareDistanceTo(vec36))) {
			vec36 = vec31;
		}

		if (vec32 != null && (vec36 == null || start.squareDistanceTo(vec32) < start.squareDistanceTo(vec36))) {
			vec36 = vec32;
		}

		if (vec33 != null && (vec36 == null || start.squareDistanceTo(vec33) < start.squareDistanceTo(vec36))) {
			vec36 = vec33;
		}

		if (vec34 != null && (vec36 == null || start.squareDistanceTo(vec34) < start.squareDistanceTo(vec36))) {
			vec36 = vec34;
		}

		if (vec35 != null && (vec36 == null || start.squareDistanceTo(vec35) < start.squareDistanceTo(vec36))) {
			vec36 = vec35;
		}

		if (vec36 == null) {
			return null;
		} else {
			EnumFacing enumfacing = null;

			if (vec36 == vec3) {
				enumfacing = EnumFacing.WEST;
			}

			if (vec36 == vec31) {
				enumfacing = EnumFacing.EAST;
			}

			if (vec36 == vec32) {
				enumfacing = EnumFacing.DOWN;
			}

			if (vec36 == vec33) {
				enumfacing = EnumFacing.UP;
			}

			if (vec36 == vec34) {
				enumfacing = EnumFacing.NORTH;
			}

			if (vec36 == vec35) {
				enumfacing = EnumFacing.SOUTH;
			}

			MovingObjectPosition mop = new MovingObjectPosition(vec36.addVector((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), enumfacing, pos);
			mop.subHit = subHit;
			return mop;
		}
	}

	private static boolean isVecInsideYZBounds(AxisAlignedBB aabb, Vec3 point) {
		return point == null ? false : point.yCoord >= aabb.minY && point.yCoord <= aabb.maxY && point.zCoord >= aabb.minZ && point.zCoord <= aabb.maxZ;
	}

	private static boolean isVecInsideXZBounds(AxisAlignedBB aabb, Vec3 point) {
		return point == null ? false : point.xCoord >= aabb.minX && point.xCoord <= aabb.maxX && point.zCoord >= aabb.minZ && point.zCoord <= aabb.maxZ;
	}

	private static boolean isVecInsideXYBounds(AxisAlignedBB aabb, Vec3 point) {
		return point == null ? false : point.xCoord >= aabb.minX && point.xCoord <= aabb.maxX && point.yCoord >= aabb.minY && point.yCoord <= aabb.maxY;
	}
}
