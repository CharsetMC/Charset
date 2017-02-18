package pl.asie.charset.lib.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
        if (boxes1.size() == 0)
            return false;

        if (world instanceof World) {
            List<AxisAlignedBB> boxes2 = new ArrayList<>();
            AxisAlignedBB collisionBox = new AxisAlignedBB(0,0,0,0,0,0);
            for (AxisAlignedBB box : boxes1) {
                collisionBox = collisionBox.union(box);
            }
            // TODO: Is false right?
            world.getBlockState(pos).addCollisionBoxToList((World) world, pos, collisionBox, boxes2, null, false);

            return intersects(boxes1, boxes2);
        } else {
            return intersects(boxes1, world.getBlockState(pos).getBoundingBox(world, pos));
        }
    }
}
