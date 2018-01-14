/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.module.tools.building;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pl.asie.charset.lib.item.ISubItemProvider;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.item.SubItemProviderRecipes;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.module.storage.barrels.CharsetStorageBarrels;

import javax.annotation.Nullable;
import java.util.Collection;

public class ItemCharsetTool extends ItemBase {
    public ItemCharsetTool() {
        super();
        setMaxStackSize(1);
    }

    public ItemMaterial getMaterial(ItemStack stack, MaterialSlot slot) {
        if (stack.hasTagCompound()) {
            NBTTagCompound cpd = stack.getTagCompound();
            if (cpd.hasKey(slot.nbtKey)) {
                return ItemMaterialRegistry.INSTANCE.getMaterial(cpd, slot.nbtKey);
            }
        }

        return getDefaultMaterial(slot);
    }

    protected ItemMaterial getDefaultMaterial(MaterialSlot slot) {
        if (slot == MaterialSlot.HANDLE) {
            return ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Items.STICK));
        } else {
            Collection<ItemMaterial> materials = ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("ingot", "iron");
            if (materials.size() > 0) {
                return materials.iterator().next();
            } else {
                materials = ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("ingot");
                if (materials.size() > 0) {
                    return materials.iterator().next();
                } else {
                    throw new RuntimeException("No default material found! D:");
                }
            }
        }
    }


    @Override
    protected ISubItemProvider createSubItemProvider() {
        return new SubItemProviderCache(new SubItemProviderRecipes(() -> this));
    }

    public enum MaterialSlot {
        HANDLE,
        HEAD;

        public final String nbtKey;

        MaterialSlot() {
            this.nbtKey = "m" + name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
        }
    }
}
