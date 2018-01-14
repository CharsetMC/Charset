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

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.lib.IBlockCapabilityProvider;
import pl.asie.charset.api.lib.ICacheable;

import java.util.List;

public class CapabilityCache extends TileCache {
    public static class Single<T> extends CapabilityCache {
        private final Capability<T> capability;
        private final EnumFacing facing;
        private ICacheable object;

        public Single(World world, BlockPos pos, boolean blocks, boolean tiles, boolean entities, Capability<T> capability, EnumFacing facing) {
            super(world, pos, blocks, tiles, entities);
            this.capability = capability;
            this.facing = facing;
        }

        @Override
        public void reload() {
            super.reload();
            hasBlockCaps = CapabilityHelper.blockProviders.contains(state.getBlock(), capability);
        }

        @SuppressWarnings("unchecked")
        public T get() {
            if (state == null) {
                object = null;
            }

            if (object != null && object.isCacheValid()) {
                return (T) object;
            }

            T result = get(capability, facing);
            if (result instanceof ICacheable) {
                object = (ICacheable) result;
            } else {
                object = null;
            }
            return result;
        }
    }

    protected final boolean blocks, tiles, entities;
    protected boolean hasBlockCaps, hasEntityCaps;

    public CapabilityCache(World world, BlockPos pos, boolean blocks, boolean tiles, boolean entities) {
        super(world, pos);
        this.blocks = blocks;
        this.tiles = tiles;
        this.entities = entities;
    }

    @Override
    public void reload() {
        super.reload();
        hasBlockCaps = CapabilityHelper.blockProviders.containsRow(state.getBlock());
        hasEntityCaps = state.isFullCube();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Capability<T> capability, EnumFacing facing) {
        getBlock();

        if (tiles && hasTile) {
            TileEntity tile = getTile();
            if (tile != null) {
                T result = CapabilityHelper.get(capability, tile, facing);
                if (result != null)
                    return result;
            }
        }

        if (blocks && hasBlockCaps) {
            IBlockCapabilityProvider provider = CapabilityHelper.blockProviders.get(state.getBlock(), capability);
            if (provider != null) {
                T result = (T) provider.create(world, pos, state, facing);
                if (result != null) {
                    return result;
                }
            }
        }

        if (entities && hasEntityCaps) {
            List<Entity> entityList = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos));
            for (Entity entity : entityList) {
                T result = CapabilityHelper.get(capability, entity, facing);
                if (result != null)
                    return result;
            }
        }

        return null;
    }
}
