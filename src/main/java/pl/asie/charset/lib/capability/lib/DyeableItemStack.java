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

package pl.asie.charset.lib.capability.lib;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.api.lib.IDyeableItem;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DyeableItemStack implements IDyeableItem, ICapabilityProvider {
    private final boolean storesAlpha;
    private final int colorCount;
    private final ItemStack stack;

    public DyeableItemStack(ItemStack stack) {
        this(stack, 1, false);
    }

    public DyeableItemStack(ItemStack stack, int count, boolean alpha) {
        this.stack = stack;
        this.colorCount = count;
        this.storesAlpha = alpha;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.DYEABLE_ITEM;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.DYEABLE_ITEM ? Capabilities.DYEABLE_ITEM.cast(this) : null;
    }

    protected String getKey(int c) {
        return getColorSlotCount() == 1 ? "color" : ("color" + c);
    }

    @Override
    public int getColorSlotCount() {
        return colorCount;
    }

    @Override
    public int getColor(int slot) {
        if (stack.hasTagCompound()) {
            NBTTagCompound cpd = stack.getTagCompound();
            String key = getKey(slot);
            if (cpd.hasKey(key, Constants.NBT.TAG_ANY_NUMERIC)) {
                return cpd.getInteger(key);
            }
        }

        return -1;
    }

    @Override
    public boolean hasColor(int slot) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(getKey(slot));
    }

    @Override
    public boolean removeColor(int slot) {
        if (stack.hasTagCompound()) {
            NBTTagCompound cpd = stack.getTagCompound();
            String key = getKey(slot);
            if (cpd.hasKey(key, Constants.NBT.TAG_ANY_NUMERIC)) {
                cpd.removeTag(key);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean setColor(int slot, int color) {
        if (!storesAlpha) color |= 0xFF000000;
        ItemUtils.getTagCompound(stack, true).setInteger(getKey(slot), color);

        return true;
    }
}
