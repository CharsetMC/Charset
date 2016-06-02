package pl.asie.charset.lib.utils;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public final class CapabilityUtils {
    private CapabilityUtils() {

    }

    public static boolean hasCapability(World world, BlockPos pos, Capability<?> capability, EnumFacing facing, boolean tiles, boolean entities) {
        if (tiles) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && tile.hasCapability(capability, facing)) {
                return true;
            }
        }

        if (entities) {
            List<Entity> entityList = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos));
            for (Entity entity : entityList) {
                if (entity.hasCapability(capability, facing)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static <T> T getCapability(World world, BlockPos pos, Capability<T> capability, EnumFacing facing, boolean tiles, boolean entities) {
        if (tiles) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && tile.hasCapability(capability, facing)) {
                return tile.getCapability(capability, facing);
            }
        }

        if (entities) {
            List<Entity> entityList = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos));
            for (Entity entity : entityList) {
                if (entity.hasCapability(capability, facing)) {
                    return entity.getCapability(capability, facing);
                }
            }
        }

        return null;
    }
}
