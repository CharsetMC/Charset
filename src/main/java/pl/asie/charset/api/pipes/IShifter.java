/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.api.pipes;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implement this interface as a capability if you want to act
 * as a Shifter, that is control the flow of items and fluids
 * in Charset pipes.
 */
@Deprecated
public interface IShifter {
	enum Mode {
		Extract,
		Shift
	}

	/**
	 * Get the mode the Shifter is currently in.
	 * @return The mode the Shifter is currently in.
     */
	@Nonnull Mode getMode();

	/**
	 * Get the direction the Shifter is pushing in.
	 * @return The direction the Shifter is pushing in.
     */
	@Nullable EnumFacing getDirection();

	/**
	 * Get the maximum shifting distance.
	 * @return The maximum shifting distance, in blocks.
     */
	int getShiftDistance();

	/**
	 * Check whether the Shifter is currently active.
	 * @return Whether or not the Shifter is currently active.
     */
	boolean isShifting();

	/**
	 * Check whether this Shifter has a filter.
	 * @return Whether or not this Shifter has a filter.
     */
	boolean hasFilter();

	/**
	 * Check if a given item stack matches the Shifter's filter.
	 * @param source The compared item stack.
	 * @return Whether or not the item stack is pushed by this Shifter.
     */
	boolean matches(ItemStack source);

	/**
	 * Check if a given fluid stack matches the Shifter's filter.
	 * @param source The compared fluid stack.
	 * @return Whether or not the fluid stack is pushed by this Shifter.
	 */
	boolean matches(FluidStack source);
}
