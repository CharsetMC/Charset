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

package pl.asie.charset.lib.capability.lib;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.api.lib.IDyeableItem;

import javax.annotation.Nullable;

public class DyeableItemStorage implements Capability.IStorage<IDyeableItem> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IDyeableItem> capability, IDyeableItem instance, EnumFacing side) {
        NBTTagCompound colors = new NBTTagCompound();
        for (int i = 0; i < instance.getColorSlotCount(); i++) {
            if (instance.hasColor(i)) {
                colors.setInteger("color" + i, instance.getColor(i));
            }
        }
        return colors;
    }

    @Override
    public void readNBT(Capability<IDyeableItem> capability, IDyeableItem instance, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound colors = (NBTTagCompound) nbt;
            for (int i = 0; i < instance.getColorSlotCount(); i++) {
                String key = "color" + i;
                if (colors.hasKey(key, Constants.NBT.TAG_ANY_NUMERIC)) {
                    instance.setColor(i, colors.getInteger(key));
                } else {
                    instance.removeColor(i);
                }
            }
        }
    }
}
