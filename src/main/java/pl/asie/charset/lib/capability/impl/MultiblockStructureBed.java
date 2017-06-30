package pl.asie.charset.lib.capability.impl;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.lib.IMultiblockStructure;

import java.util.Iterator;

public class MultiblockStructureBed implements IMultiblockStructure {
    private final TileEntityBed bed;

    public MultiblockStructureBed(TileEntityBed bed) {
        this.bed = bed;
    }

    private BlockPos getNeighborPos() {
        World world = bed.getWorld();
        IBlockState state = world.getBlockState(bed.getPos());
        EnumFacing facing = state.getValue(BlockBed.FACING);

        if (state.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) return bed.getPos().offset(facing);
        else return bed.getPos().offset(facing.getOpposite());
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return ImmutableList.of(bed.getPos(), getNeighborPos()).iterator();
    }

    @Override
    public boolean contains(BlockPos pos) {
        return pos.equals(bed.getPos()) || pos.equals(getNeighborPos());
    }

    @Override
    public boolean isSeparable() {
        return false;
    }
}
