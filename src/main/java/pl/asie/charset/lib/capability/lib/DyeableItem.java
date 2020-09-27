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

package pl.asie.charset.lib.capability.lib;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.api.lib.IDyeableItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DyeableItem implements IDyeableItem, ICapabilityProvider {
    private final boolean storesAlpha;
    private final boolean[] colorSet;
    private final int[] colors;

    public DyeableItem() {
        this(1, false);
    }

    public DyeableItem(int count, boolean alpha) {
        this.colorSet = new boolean[count];
        this.colors = new int[count];
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

    @Override
    public int getColorSlotCount() {
        return colors.length;
    }

    @Override
    public int getColor(int slot) {
        if (hasColor(slot)) {
            return colors[slot];
        } else {
            return -1;
        }
    }

    @Override
    public boolean hasColor(int slot) {
        return colorSet[slot];
    }

    @Override
    public boolean removeColor(int slot) {
        if (colorSet[slot]) {
            colorSet[slot] = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean setColor(int slot, int color) {
        colorSet[slot] = true;
        if (!storesAlpha) color |= 0xFF000000;
        colors[slot] = color;
        return true;
    }
}
