/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.wires;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartCapabilityHelper;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumEdgeSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.world.IMultipartBlockAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.modcompat.mcmultipart.MCMPUtils;
import pl.asie.charset.lib.utils.OcclusionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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

    public static boolean hasCapability(Wire wire, BlockPos pos, Capability<?> capability, EnumFacing face, boolean ignoreWires) {
        TileWire.isWireCheckingForCaps = ignoreWires;
        if (wire.getLocation() != WireFace.CENTER) {
            Optional<IMultipartContainer> container = MultipartHelper.getContainer(wire.getContainer().world(), pos);
            if (container.isPresent()) {
            	boolean result = MCMPUtils.streamParts(container.get(), wire.getLocation().facing, face).anyMatch((info) -> info.getTile().hasPartCapability(capability, face));
                TileWire.isWireCheckingForCaps = false;
                return result;
            }
        }

        TileEntity tile = wire.getContainer().world().getTileEntity(pos);
        boolean result = tile != null && tile.hasCapability(capability, face);
        TileWire.isWireCheckingForCaps = false;
        return result;
    }

    public static ICapabilityProvider getCapabilityProvider(Wire searcher, BlockPos pos, boolean ignoreWires) {
        TileEntity tile = searcher.getContainer().world().getTileEntity(pos);
        if (tile != null) {
            return tile;
        } else {
            return null;
        }
    }

    public static <T> T getCapability(Wire searcher, BlockPos pos, Capability<T> capability, EnumFacing face, boolean ignoreWires) {
        TileWire.isWireCheckingForCaps = ignoreWires;

        // for non-center wires, use multiparts to check for potential edge connections
        if (searcher.getLocation() != WireFace.CENTER) {
            Optional<IMultipartContainer> container = MultipartHelper.getContainer(searcher.getContainer().world(), pos);
            if (container.isPresent()) {
                T result = MCMPUtils.streamParts(container.get(), searcher.getLocation().facing, face).filter((info) -> info.getTile().hasPartCapability(capability, face))
		                .map((info) -> info.getTile().getPartCapability(capability, face)).findFirst().orElse(null);
                TileWire.isWireCheckingForCaps = false;
                return result;
            }
        }

        TileEntity tile = searcher.getContainer().world().getTileEntity(pos);
        T result = tile != null ? tile.getCapability(capability, face) : null;
        TileWire.isWireCheckingForCaps = false;
        return result;
    }

    public static boolean canConnectInternal(Wire wire, WireFace side) {
        if (side == wire.getLocation()) {
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

        AxisAlignedBB mask = wire.getProvider().getCornerCollisionBox(wire.getLocation(), direction.getOpposite());
        if (OcclusionUtils.INSTANCE.intersects(Collections.singletonList(mask), wire.getContainer().world(), middlePos)) {
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
        IMultipartContainer container = null;
        if (access instanceof IMultipartBlockAccess) {
            access = ((IMultipartBlockAccess) access).getActualWorld();
        }

        Optional<IMultipartContainer> containerOpt = MultipartHelper.getContainer(access, pos);
        if (containerOpt.isPresent()) {
            container = containerOpt.get();
        }

        if (container != null) {
            Optional<IMultipartTile> tile = container.getPartTile(toPartSlot(face));
            if (tile.isPresent() && tile.get().getTileEntity() instanceof TileWire) {
                Wire wire = ((TileWire) tile.get().getTileEntity()).wire;
                if (wire != null && wire.getLocation() == face) {
                    return ((TileWire) tile.get().getTileEntity()).wire;
                }
            }

            return null;
        }

        TileEntity tile = access.getTileEntity(pos);
        if (tile instanceof TileWire && ((TileWire) tile).wire != null && ((TileWire) tile).wire.getLocation() == face) {
            return ((TileWire) tile).wire;
        } else {
            return null;
        }
    }

    public static @Nullable Wire getAnyWire(IBlockAccess access, BlockPos pos) {
        if (access instanceof IMultipartBlockAccess) {
            IPartInfo info = ((IMultipartBlockAccess) access).getPartInfo();
            if (info.getPartPos().equals(pos)) {
                return getAnyWire(info.getTile().getTileEntity());
            }
        }

        return getAnyWire(access.getTileEntity(pos));
    }

    public static Collection<Wire> getAllWires(IBlockAccess access, BlockPos pos) {
        Optional<IMultipartContainer> containerOpt = MultipartHelper.getContainer(access, pos);
        if (containerOpt.isPresent()) {
            IMultipartContainer container = containerOpt.get();
            Collection<Wire> wires = new ArrayList<>();
            for (IPartInfo partInfo : container.getParts().values()) {
                if (partInfo.getTile() instanceof TileWire) {
                    wires.add(((TileWire) partInfo.getTile()).wire);
                }
            }

            return wires;
        } else {
            TileEntity tile = access.getTileEntity(pos);
            if (tile instanceof TileWire) {
                return Collections.singleton(((TileWire) tile).wire);
            } else {
                return Collections.emptySet();
            }
        }
    }

    public static @Nullable Wire getAnyWire(TileEntity tile) {
        if (tile != null && tile instanceof TileWire) {
            return ((TileWire) tile).wire;
        } else {
            return null;
        }
    }

    public static IPartSlot toPartSlot(WireFace face) {
        return face == WireFace.CENTER ? EnumCenterSlot.CENTER : EnumFaceSlot.fromFace(face.facing);
    }
}
