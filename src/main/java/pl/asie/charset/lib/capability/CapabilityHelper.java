package pl.asie.charset.lib.capability;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pl.asie.charset.api.lib.IBlockCapabilityProvider;

import java.util.List;

public final class CapabilityHelper {
    public interface Wrapper<T> {
        T get(ICapabilityProvider provider, EnumFacing side);
    }

    protected static final Table<Block, Capability, IBlockCapabilityProvider> blockProviders = HashBasedTable.create();
    private static final Multimap<Capability, Wrapper<?>> wrappers = LinkedListMultimap.create();

    private CapabilityHelper() {

    }

    public static <T> void registerBlockProvider(Capability<T> capability, Block block, IBlockCapabilityProvider<T> provider) {
        if (blockProviders.contains(block, capability)) {
            throw new RuntimeException("Capability provider already exists for pair (" + capability.getName() + ", " + block.toString() + ")!");
        } else {
            blockProviders.put(block, capability, provider);
        }
    }

    public static <T> void registerWrapper(Capability<T> capability, Wrapper<T> provider) {
        wrappers.put(capability, provider);
    }

    public static <T> boolean has(Capability<T> capability, ICapabilityProvider provider, EnumFacing facing) {
        return has(capability, provider, facing, true);
    }

    @SuppressWarnings("unchecked")
    public static <T> boolean has(Capability<T> capability, ICapabilityProvider provider, EnumFacing facing, boolean withWrappers) {
        if (provider != null) {
            if (provider.hasCapability(capability, facing)) {
                return true;
            }

            if (withWrappers && wrappers.containsKey(capability)) {
                for (Wrapper helper : wrappers.get(capability)) {
                    T result = (T) helper.get(provider, facing);
                    if (result != null) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public static <T> T get(Capability<T> capability, ICapabilityProvider provider, EnumFacing facing) {
        return get(capability, provider, facing, true);
    }

    @SuppressWarnings("unchecked")
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

    public static <T> boolean hasBlockCapability(Capability<T> capability, IBlockState state) {
        return blockProviders.contains(state.getBlock(), capability);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBlockCapability(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing facing, Capability<T> capability) {
        IBlockCapabilityProvider provider = blockProviders.get(state.getBlock(), capability);
        if (provider != null) {
            T result = (T) provider.create(world, pos, state, facing);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> boolean has(World world, BlockPos pos, Capability<T> capability, EnumFacing facing, boolean blocks, boolean tiles, boolean entities) {
        IBlockState state = world.getBlockState(pos);

        if (tiles && state.getBlock().hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            if (has(capability, tile, facing))
                return true;
        }

        if (blocks) {
            IBlockCapabilityProvider provider = blockProviders.get(state.getBlock(), capability);
            if (provider != null) {
                return true;
            }
        }

        if (entities && !world.isSideSolid(pos, facing, false)) {
            List<Entity> entityList = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos));
            for (Entity entity : entityList) {
                if (has(capability, entity, facing))
                    return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(World world, BlockPos pos, Capability<T> capability, EnumFacing facing, boolean blocks, boolean tiles, boolean entities) {
        IBlockState state = world.getBlockState(pos);

        if (tiles && state.getBlock().hasTileEntity(state)) {
            TileEntity tile = world.getTileEntity(pos);
            T result = get(capability, tile, facing);
            if (result != null)
                return result;
        }

        if (blocks) {
            IBlockCapabilityProvider provider = blockProviders.get(state.getBlock(), capability);
            if (provider != null) {
                T result = (T) provider.create(world, pos, state, facing);
                if (result != null) {
                    return result;
                }
            }
        }

        if (entities && !world.isSideSolid(pos, facing, false)) {
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
