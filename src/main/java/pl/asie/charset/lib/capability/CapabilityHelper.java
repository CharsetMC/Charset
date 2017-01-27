package pl.asie.charset.lib.capability;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

public final class CapabilityHelper {
    public interface Wrapper<T> {
        T get(ICapabilityProvider provider, EnumFacing side);
    }

    private static final Multimap<Capability, Wrapper<?>> wrappers = LinkedListMultimap.create();

    private CapabilityHelper() {

    }

    public static <T> void registerWrapper(Capability<T> capability, Wrapper<T> provider) {
        wrappers.put(capability, provider);
    }

    public static <T> T get(Capability<T> capability, ICapabilityProvider provider, EnumFacing facing) {
        return get(capability, provider, facing, true);
    }

    public static <T> T get(Capability<T> capability, ICapabilityProvider provider, EnumFacing facing, boolean withWrappers) {
        if (provider != null) {
            if (provider.hasCapability(capability, facing)) {
                return provider.getCapability(capability, facing);
            }

            if (withWrappers && wrappers.containsKey(capability)) {
                for (Wrapper helper : wrappers.get(capability)) {
                    T result = (T) helper.get(provider, facing);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    public static <T> T get(World world, BlockPos pos, Capability<T> capability, EnumFacing facing, boolean tiles, boolean entities) {
        if (tiles) {
            TileEntity tile = world.getTileEntity(pos);
            T result = get(capability, tile, facing);
            if (result != null)
                return result;
        }

        if (entities) {
            List<Entity> entityList = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos));
            for (Entity entity : entityList) {
                T result = get(capability, entity, facing);
                if (result != null)
                    return result;
            }
        }

        return null;
    }
}
