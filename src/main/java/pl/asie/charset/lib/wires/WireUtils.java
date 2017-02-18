package pl.asie.charset.lib.wires;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import pl.asie.charset.api.wires.WireFace;

import javax.annotation.Nullable;

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

    public static boolean canConnectInternal(Wire wire, WireFace side) {
        WireFace location = wire.location;

        if (side == location) {
            return false;
        }

        Wire wire2 = getWire(wire.world, wire.pos, side);
        return wire2 != null && wire2.canConnectWire(wire);
    }

    public static boolean canConnectExternal(Wire wire, EnumFacing facing) {
        BlockPos pos2 = wire.pos.offset(facing);
        Wire wire2 = getWire(wire.world, pos2, wire.location);

        if (wire2 != null) {
            if (wire2.isOccluded(facing.getOpposite())) {
                return false;
            }

            return wire2.canConnectWire(wire);
        } else {
            return wire.canConnectBlock(pos2, facing.getOpposite());
        }
    }

    public static boolean canConnectCorner(Wire wire, EnumFacing direction) {
        if (wire.location == WireFace.CENTER || wire.isCornerOccluded(direction)) {
            return false;
        }

        EnumFacing side = wire.location.facing;
        BlockPos middlePos = wire.pos.offset(direction);

        if (wire.world.isSideSolid(middlePos, direction.getOpposite()) || wire.world.isSideSolid(middlePos, side.getOpposite())) {
            return false;
        }

        BlockPos cornerPos = middlePos.offset(side);
        Wire wire2 = getWire(wire.world, cornerPos, WireFace.get(direction.getOpposite()));

        if (wire2 == null || wire2.isCornerOccluded(side.getOpposite()) || !wire2.canConnectWire(wire)) {
            return false;
        }

        return true;
    }

    public static @Nullable Wire getWire(IBlockAccess access, BlockPos pos, WireFace face) {
        // TODO
        return null;
    }
}
