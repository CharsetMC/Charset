package pl.asie.charset.api.lib;

import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public interface IMultiblockStructure {
    /**
     * Iterate over all the positions this multiblock structure includes.
     */
    Iterator<BlockPos> iterator();

    /**
     * Checks whether a position is part of this multiblock structure.
     * @param pos The given position.
     * @return Whether the given position is part of this multiblock structure.
     */
    boolean contains(BlockPos pos);

    /**
     * Returns true if the structure is separable; that is, removing or
     * moving one block has no destructive effect on the structure as a whole.
     *
     * For example, chests and BC-style tanks are considered separable.
     * Beds are not considered separable. However, it's not easy to draw a line
     * between what counts as separable and not; most structures which store
     * information in a central "controller" block would be inseparable, whereas
     * most structures which store information in each relevant part would be
     * separable. Structures which destroy other blocks when separated (like
     * vanilla beds) are, however, definitely inseparable.
     *
     * @return Whether the structure is separable or not.
     */
    default boolean isSeparable() { return true; }
}

