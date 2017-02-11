package pl.asie.charset.api.lib;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMovable {
    /**
     * @return Whether this object can be moved at this time.
     * For example, whether it can be picked up for carrying.
     */
    boolean canMoveFrom();

    /**
     * @return Whether this object can be moved to this location.
     */
    boolean canMoveTo(World world, BlockPos pos);
}
