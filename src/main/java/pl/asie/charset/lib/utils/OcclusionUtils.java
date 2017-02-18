package pl.asie.charset.lib.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Collection;
import java.util.Collections;

public class OcclusionUtils {
    public static OcclusionUtils INSTANCE = new OcclusionUtils();

    protected OcclusionUtils() {

    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, Collection<AxisAlignedBB> boxes2) {
        return boxes1.stream().anyMatch(b1 -> boxes2.stream().anyMatch(b1::intersectsWith));
    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, AxisAlignedBB box2) {
        return boxes1.stream().anyMatch(box2::intersectsWith);
    }

    public boolean intersects(Collection<AxisAlignedBB> boxes1, IBlockAccess world, BlockPos pos) {
        return intersects(boxes1, world.getBlockState(pos).getBoundingBox(world, pos));
    }
}
