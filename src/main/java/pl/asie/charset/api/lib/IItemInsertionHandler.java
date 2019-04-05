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

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

/**
 * This capability is present for objects (primarily tiles) which may want to
 * allow injection, but do not want to be considered inventories (such as
 * Charset pipes).
 */
public interface IItemInsertionHandler {
	/**
	 * Tries to inject an item into the pipe.
	 *
	 * @param stack    The stack offered for injection.
	 * @param simulate If true, no actual injection should take place.
	 * @return The remaining ItemStack that was not inserted.
	 */
	ItemStack insertItem(ItemStack stack, boolean simulate);

	// For pipes which dye items. Color-coding.
	default ItemStack insertItem(ItemStack stack, EnumDyeColor color, boolean simulate) {
		return insertItem(stack, simulate);
	}
}
