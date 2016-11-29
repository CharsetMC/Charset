package pl.asie.charset.lib.capability;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class CapabilityHelper {
    public interface Provider<T> {
        T get(ICapabilityProvider provider, EnumFacing side);
    }

    private static final Map<Capability, Provider<?>> providers = new IdentityHashMap<>();

    private CapabilityHelper() {

    }

    public static <T> void registerProvider(Capability<T> capability, Provider<T> provider) {
        providers.put(capability, provider);
    }

    public static <T> T get(Capability<T> capability, ICapabilityProvider provider, EnumFacing facing) {
        return get(capability, provider, facing, true);
    }

    public static <T> T get(Capability<T> capability, ICapabilityProvider provider, EnumFacing facing, boolean withWrappers) {
        if (provider == null)
            return null;

        if (withWrappers && providers.containsKey(capability)) {
            return (T) providers.get(capability).get(provider, facing);
        } else {
            if (provider.hasCapability(capability, facing)) {
                return provider.getCapability(capability, facing);
            } else {
                return null;
            }
        }
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
