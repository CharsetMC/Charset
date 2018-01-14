/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.api.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IBarrel {
    int getItemCount();
    int getMaxItemCount();
    boolean containsUpgrade(String upgradeName);

    // The following indicate behaviour of barrel IItemHandlers.
    // Please respect them if applicable, though they are not
    // enforced.
    boolean shouldExtractFromSide(EnumFacing side);
    boolean shouldInsertToSide(EnumFacing side);

    // The following two methods follow the IItemHandler contract,
    // except they do not cap out at the item's maximum stack size.
    ItemStack extractItem(int maxCount, boolean simulate);
    ItemStack insertItem(ItemStack stack, boolean simulate);
}
