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
