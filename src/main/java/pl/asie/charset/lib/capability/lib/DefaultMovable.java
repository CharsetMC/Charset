package pl.asie.charset.lib.capability.lib;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.lib.IMovable;

public class DefaultMovable implements IMovable {
    @Override
    public boolean canMoveFrom() {
        return false;
    }

    @Override
    public boolean canMoveTo(World world, BlockPos pos) {
        return false;
    }
}
