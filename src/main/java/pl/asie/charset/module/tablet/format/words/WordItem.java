/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tablet.format.words;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.module.tablet.format.api.Word;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WordItem extends Word {
    private final ItemStack errorStack = new ItemStack(Blocks.FIRE);
    private final List<ItemStack> entries;

    public WordItem(Collection<ItemStack> entries) {
        this.entries = expand(entries);
    }

    private List<ItemStack> expand(Collection<ItemStack> stacks) {
        List<ItemStack> newList = new ArrayList<>();

        for (ItemStack is : stacks) {
            if (is.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                NonNullList<ItemStack> out = NonNullList.create();
                is.getItem().getSubItems(CreativeTabs.SEARCH, out);
                newList.addAll(expand(out));
            } else {
                newList.add(is);
            }
        }

        return newList;
    }

    private int activeItemIndex;

    public ItemStack getItem() {
        activeItemIndex = 0;
        if (entries.size() == 0) {
            return errorStack;
        }

        long now = System.currentTimeMillis() / 1000;
        now %= entries.size();
        activeItemIndex = (int) now;
        return entries.get(activeItemIndex);
    }

    public void onItemErrored(Throwable t) {
        t.printStackTrace();
        if (entries != null && activeItemIndex < entries.size()) {
            entries.set(activeItemIndex, errorStack);
        }
    }
}
