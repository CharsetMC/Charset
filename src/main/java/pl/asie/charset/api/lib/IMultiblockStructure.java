/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

