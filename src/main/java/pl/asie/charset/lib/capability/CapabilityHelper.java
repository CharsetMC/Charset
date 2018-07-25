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
            throw new RuntimeException("CapabilityImpl provider already exists for pair (" + capability.getName() + ", " + block.toString() + ")!");
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
    public static <T> boolean has(IBlockAccess world, BlockPos pos, Capability<T> capability, EnumFacing facing, boolean blocks, boolean tiles, boolean entities) {
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

        if (world instanceof World) {
            if (entities && !world.isSideSolid(pos, facing, false)) {
                List<Entity> entityList = ((World) world).getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos));
                for (Entity entity : entityList) {
                    if (has(capability, entity, facing))
                        return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(IBlockAccess world, BlockPos pos, Capability<T> capability, EnumFacing facing, boolean blocks, boolean tiles, boolean entities) {
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

        if (world instanceof World) {
            if (entities && !world.isSideSolid(pos, facing, false)) {
                List<Entity> entityList = ((World) world).getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos));
                for (Entity entity : entityList) {
                    T result = get(capability, entity, facing);
                    if (result != null)
                        return result;
                }
            }
        }

        return null;
    }
}
