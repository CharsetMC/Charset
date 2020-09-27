/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubItemProviderCache implements ISubItemProvider {
    private static final Set<SubItemProviderCache> CACHES = new HashSet<>();
    private final ISubItemProvider parent;
    private List<ItemStack> items;

    public SubItemProviderCache(ISubItemProvider parent) {
        CACHES.add(this);
        this.parent = parent;
    }

    public static void clear() {
        for (SubItemProviderCache cache : CACHES) {
            cache.items = null;
        }
    }

    @Override
    public List<ItemStack> getItems() {
        if (items != null)
            return items;

        List<ItemStack> genItems = parent.getItems();
        items = genItems;
        return items;
    }

    @Override
    public List<ItemStack> getAllItems() {
        return parent.getAllItems();
    }
}
