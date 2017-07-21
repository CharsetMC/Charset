package pl.asie.charset.lib.capability.impl;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import pl.asie.charset.api.lib.IMultiblockStructure;

import java.util.Iterator;

public class MultiblockStructureDoor implements IMultiblockStructure {
    private final World world;
    private final BlockPos pos;
    private final IBlockState state;

    public MultiblockStructureDoor(World world, BlockPos pos, IBlockState state) {
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
