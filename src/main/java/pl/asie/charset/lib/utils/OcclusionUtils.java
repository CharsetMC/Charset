package pl.asie.charset.lib.utils;

import net.minecraft.block.Block;
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
            AxisAlignedBB collisionBox = null;
            for (AxisAlignedBB box : boxes1) {
                if (collisionBox == null) {
                    collisionBox = box;
                } else {
                    collisionBox = collisionBox.union(box);
                }
            }

            // TODO: Is false right?
            world.getBlockState(pos).addCollisionBoxToList((World) world, pos, collisionBox.offset(pos), boxes2, null, false);
            if (boxes2.size() > 0) {
                BlockPos negPos = new BlockPos(-pos.getX(), -pos.getY(), -pos.getZ());
                for (int i = 0; i < boxes2.size(); i++) {
                    boxes2.set(i, boxes2.get(i).offset(negPos));
                }

                return intersects(boxes1, boxes2);
            } else {
                return false;
            }
        } else {
            return intersects(boxes1, world.getBlockState(pos).getBoundingBox(world, pos));
        }
    }
}
