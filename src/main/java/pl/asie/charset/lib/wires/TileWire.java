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

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.ref.MCMPCapabilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.block.TileBase;
import pl.asie.charset.lib.scheduler.Scheduler;

public class TileWire extends TileBase implements IMultipartTile, ITickable, IWireContainer {
    protected Wire wire;

    @Override
    public void update() {
        super.update();
        if (wire != null) {
            wire.update();
        } else {
            // Modifying it instantly will cause a CME in MCMultiPart.
            // We only get here upon module removal anyway, so...
            Scheduler.INSTANCE.in(world, 0, () -> world.setBlockToAir(getPos()));
        }
    }

    @Override
    public void readNBTData(NBTTagCompound nbt, boolean isClient) {
        if (nbt.hasKey("f")) {
            WireProvider factory = WireManager.REGISTRY.getValue(nbt.getByte("f"));
            if (factory != null) {
                WireFace location = WireFace.VALUES[nbt.getByte("l")];
                wire = factory.create(this, location);
                wire.readNBTData(nbt, isClient);
            }
            if (isClient) {
                markBlockForRenderUpdate();
            }
        } else {
            if (wire != null) {
                wire = null;
                if (isClient) {
                    markBlockForRenderUpdate();
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeNBTData(NBTTagCompound nbt, boolean isClient) {
        if (wire != null) {
            nbt.setByte("f", (byte) WireManager.REGISTRY.getID(wire.getProvider()));
            nbt.setByte("l", (byte) wire.getLocation().ordinal());
            nbt = wire.writeNBTData(nbt, isClient);
        }
        return nbt;
    }

    @Override
    public ItemStack getDroppedBlock(IBlockState state) {
        if (wire != null) {
            return wire.getProvider().getItemWire().toStack(wire.getLocation() == WireFace.CENTER, 1);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void onPlacedBy(WireFace facing, ItemStack stack) {
        wire = ((ItemWire) stack.getItem()).fromStack(this, stack, facing.facing);
        wire.onChanged(true);
        markBlockForUpdate();
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public BlockPos pos() {
        return pos;
    }

    @Override
    public void requestNeighborUpdate(int connectionMask) {
        CharsetLibWires.blockWire.requestNeighborUpdate(world, pos, wire.getLocation(), connectionMask);
    }

    @Override
    public void requestNetworkUpdate() {
        markBlockForUpdate();
    }

    @Override
    public void requestRenderUpdate() {
        requestNetworkUpdate();
        markBlockForRenderUpdate();
    }

    @Override
    public void dropWire() {
        Block.spawnAsEntity(world, pos, getDroppedBlock(world.getBlockState(pos)));
        world.setBlockToAir(pos);
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    // I'm a horrible, horrible dev
    protected static boolean isWireCheckingForCaps;

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == MCMPCapabilities.MULTIPART_TILE || (wire != null && !isWireCheckingForCaps && wire.hasCapability(capability, facing)) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MCMPCapabilities.MULTIPART_TILE) {
            return MCMPCapabilities.MULTIPART_TILE.cast(this);
        }

        if (wire != null && !isWireCheckingForCaps) {
            T result = wire.getCapability(capability, facing);
            if (result != null) {
                return result;
            }
        }

        return super.getCapability(capability, facing);
    }
}
