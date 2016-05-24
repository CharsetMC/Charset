/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
