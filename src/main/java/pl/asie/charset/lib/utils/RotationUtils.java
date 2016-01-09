package pl.asie.charset.lib.utils;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public final class RotationUtils {
    private RotationUtils() {

    }

    public static AxisAlignedBB rotateFace(AxisAlignedBB box, EnumFacing facing) {
        switch (facing) {
            case DOWN:
            default:
                return box;
            case UP:
                return AxisAlignedBB.fromBounds(box.minX, 1 - box.maxY, box.minZ, box.maxX, 1 - box.minY, box.maxZ);
            case NORTH:
                return AxisAlignedBB.fromBounds(box.minX, box.minZ, box.minY, box.maxX, box.maxZ, box.maxY);
            case SOUTH:
                return AxisAlignedBB.fromBounds(box.minX, box.minZ, 1 - box.maxY, box.maxX, box.maxZ, 1 - box.minY);
            case WEST:
                return AxisAlignedBB.fromBounds(box.minY, box.minZ, box.minX, box.maxY, box.maxZ, box.maxX);
            case EAST:
                return AxisAlignedBB.fromBounds(1 - box.maxY, box.minZ, box.minX, 1 - box.minY, box.maxZ, box.maxX);
        }
    }
}
