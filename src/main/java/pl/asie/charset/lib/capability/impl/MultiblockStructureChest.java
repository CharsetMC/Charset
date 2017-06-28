package pl.asie.charset.lib.capability.impl;

import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.api.lib.IMultiblockStructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiblockStructureChest implements IMultiblockStructure {
    private final TileEntityChest chest;

    public MultiblockStructureChest(TileEntityChest chest) {
        this.chest = chest;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        List<BlockPos> list = new ArrayList<>(5);

        list.add(chest.getPos());
        if (chest.adjacentChestXNeg != null) list.add(chest.adjacentChestXNeg.getPos());
        if (chest.adjacentChestXPos != null) list.add(chest.adjacentChestXPos.getPos());
        if (chest.adjacentChestZNeg != null) list.add(chest.adjacentChestZNeg.getPos());
        if (chest.adjacentChestZPos != null) list.add(chest.adjacentChestZPos.getPos());

        return list.iterator();
    }

    @Override
    public boolean contains(BlockPos pos) {
        if (pos.equals(chest.getPos())) {
            return true;
        }

        if (chest.adjacentChestXNeg != null && chest.adjacentChestXNeg.getPos().equals(pos)) return true;
        if (chest.adjacentChestXPos != null && chest.adjacentChestXPos.getPos().equals(pos)) return true;
        if (chest.adjacentChestZNeg != null && chest.adjacentChestZNeg.getPos().equals(pos)) return true;
        if (chest.adjacentChestZPos != null && chest.adjacentChestZPos.getPos().equals(pos)) return true;

        return false;
    }
}
