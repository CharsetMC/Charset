package pl.asie.charset.lib.wires;

import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.tileentity.TileEntity;
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
        WireFace location = wire.getLocation();

        if (side == location) {
            return false;
        }

        Wire wire2 = getWire(wire.getContainer().world(), wire.getContainer().pos(), side);
        return wire2 != null && wire2.canConnectWire(wire);
    }

    public static boolean canConnectExternal(Wire wire, EnumFacing facing) {
        BlockPos pos2 = wire.getContainer().pos().offset(facing);
        Wire wire2 = getWire(wire.getContainer().world(), pos2, wire.getLocation());

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
        if (wire.getLocation() == WireFace.CENTER || wire.isCornerOccluded(direction)) {
            return false;
        }

        EnumFacing side = wire.getLocation().facing;
        BlockPos middlePos = wire.getContainer().pos().offset(direction);

        if (wire.getContainer().world().isSideSolid(middlePos, direction.getOpposite()) || wire.getContainer().world().isSideSolid(middlePos, side.getOpposite())) {
            return false;
        }

        BlockPos cornerPos = middlePos.offset(side);
        Wire wire2 = getWire(wire.getContainer().world(), cornerPos, WireFace.get(direction.getOpposite()));

        if (wire2 == null || wire2.isCornerOccluded(side.getOpposite()) || !wire2.canConnectWire(wire)) {
            return false;
        }

        return true;
    }

    public static @Nullable Wire getWire(IBlockAccess access, BlockPos pos, WireFace face) {
        TileEntity tile = access.getTileEntity(pos);
        if (tile != null) {
            IWireProxy proxy = tile.getCapability(CharsetLibWires.WIRE_CAP, face.facing);
            return proxy instanceof Wire && ((Wire) proxy).getLocation() == face ? (Wire) proxy : null;
        } else {
            return null;
        }
    }

    public static @Nullable Wire getAnyWire(IBlockAccess access, BlockPos pos) {
        return getAnyWire(access.getTileEntity(pos));
    }

    public static @Nullable Wire getAnyWire(TileEntity tile) {
        if (tile != null) {
            IWireProxy proxy = tile.getCapability(CharsetLibWires.WIRE_CAP, null);
            return proxy instanceof Wire ? (Wire) proxy : null;
        } else {
            return null;
        }
    }

    public static IPartSlot toPartSlot(WireFace face) {
        return face == WireFace.CENTER ? EnumCenterSlot.CENTER : EnumFaceSlot.fromFace(face.facing);
    }
}
