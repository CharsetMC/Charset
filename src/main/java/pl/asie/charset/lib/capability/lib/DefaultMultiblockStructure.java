package pl.asie.charset.lib.capability.lib;

import net.minecraft.util.math.BlockPos;
import pl.asie.charset.api.lib.IMultiblockStructure;

import java.util.Iterator;

public class DefaultMultiblockStructure implements IMultiblockStructure {
    @Override
    public Iterator<BlockPos> iterator() {
        return new Iterator<BlockPos>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public BlockPos next() {
                return null;
            }
        };
    }

    @Override
    public boolean contains(BlockPos pos) {
        return false;
    }
}
