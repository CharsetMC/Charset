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

package pl.asie.charset.module.storage.locks;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.api.storage.IKeyItem;

import java.util.List;

public class ItemKey extends ItemLockingDyeable implements IKeyItem {
    static final boolean DEBUG_KEY_ID = false;

    public ItemKey() {
        super();
        setMaxStackSize(1);
        setUnlocalizedName("charset.key");
    }

    // ""security""
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("key")) {
            NBTTagCompound tag = stack.getTagCompound().copy();
            tag.removeTag("key");
            return tag;
        } else {
            return stack.getTagCompound();
        }
    }

    public String getKey(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("key") ? stack.getTagCompound().getString("key") : "null";
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        ItemStack result = stack.copy();
        if (result.getCount() < 1) {
            result.setCount(1);
        }
        return result;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (ItemKey.DEBUG_KEY_ID) {
            tooltip.add(getKey(stack));
        }
    }

    @Override
    public boolean canUnlock(String lock, ItemStack stack) {
        return getKey(stack).equals(lock);
    }
}
