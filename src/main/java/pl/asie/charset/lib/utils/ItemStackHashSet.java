/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.utils;

import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackHashSet extends TCustomHashSet<ItemStack> {
    public static class Strategy implements HashingStrategy<ItemStack> {
        private final boolean matchStackSize, matchDamage, matchNBT;

        public Strategy(boolean matchStackSize, boolean matchDamage, boolean matchNBT) {
            this.matchStackSize = matchStackSize;
            this.matchDamage = matchDamage;
            this.matchNBT = matchNBT;
        }

        @Override
        public int computeHashCode(ItemStack object) {
            int i = Item.getIdFromItem(object.getItem());
            i = 31 * i + object.getItemDamage();
            if (object.hasTagCompound()) {
                i = 7 * i + object.getTagCompound().hashCode();
            }
            return i;
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return ItemUtils.equals(o1, o2, matchStackSize, matchDamage, matchNBT);
        }
    }

    public ItemStackHashSet(boolean matchStackSize, boolean matchDamage, boolean matchNBT) {
        super(new Strategy(matchStackSize, matchDamage, matchNBT));
    }
}