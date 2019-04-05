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

package pl.asie.charset.lib.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.CharsetLib;

import java.util.*;

public abstract class SubItemProviderSets implements ISubItemProvider {
    protected abstract int compareSets(List<ItemStack> first, List<ItemStack> second);

    protected List<ItemStack> createForcedItems() {
        return Collections.emptyList();
    }

    protected abstract List<List<ItemStack>> createItemSets();

    protected int getVisibleSetAmount() {
        return 1;
    }

    private List<ItemStack> getItems(boolean all) {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        builder.addAll(createForcedItems());
        List<List<ItemStack>> sets = createItemSets();

        if (sets.size() > 0) {
            if (all || getVisibleSetAmount() >= sets.size()) {
                sets.sort(this::compareSets);
                for (Collection<ItemStack> set : sets) {
                    if (sets.isEmpty()) {
                        ModCharset.logger.warn("Found empty set while running " + getClass().getName() + ".getItems()!");
                    }
                    builder.addAll(set);
                }
            } else {
                Calendar cal = CharsetLib.calendar.get();
                int doy = cal != null ? ((cal.get(Calendar.YEAR) * 366) + cal.get(Calendar.DAY_OF_YEAR) - 1) : 0 /* start at 0, not 1 */;
                Collections.shuffle(sets, new Random(doy));
                sets.sort(this::compareSets);
                for (int i = 0; i < Math.min(getVisibleSetAmount(), sets.size()); i++) {
                    if (sets.isEmpty()) {
                        ModCharset.logger.warn("Found empty set while running " + getClass().getName() + ".getItems()!");
                    }
                    builder.addAll(sets.get(i));
                }
            }
        }

        return builder.build();
    }

    @Override
    public List<ItemStack> getItems() {
        return getItems(CharsetLib.showAllItemTypes);
    }

    @Override
    public List<ItemStack> getAllItems() {
        return getItems(true);
    }
}
