package pl.asie.charset.lib.wires;

import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.wires.WireFace;

public final class WireUtils {
    private static final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][]{
            {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH},
            {EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
    };

    private WireUtils() {

    }

    public static EnumFacing[] getConnectionsForRender(WireFace face) {
        return CONNECTION_DIRS[face.ordinal()];
    }
}
