package pl.asie.charset.lib.capability.impl;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.api.lib.IMultiblockStructure;

import java.util.Iterator;

public class MultiblockStructureDoor implements IMultiblockStructure {
    private final IBlockAccess world;
    private final BlockPos pos;
    private final IBlockState state;

    public MultiblockStructureDoor(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing facing) {
        this.world = world;
        this.pos = pos;
        this.state = state;
    }

    private BlockPos getNeighborPos() {
        if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER) {
            return pos.down();
        } else {
            return pos.up();
        }
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return ImmutableList.of(this.pos, getNeighborPos()).iterator();
    }

    @Override
    public boolean contains(BlockPos pos) {
        return pos.equals(this.pos) || pos.equals(getNeighborPos());
    }

    @Override
    public boolean isSeparable() {
        return false;
    }
}
