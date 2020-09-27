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

package pl.asie.charset.module.storage.locks;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.lib.DyeableItemStack;
import pl.asie.charset.api.lib.IDyeableItem;
import pl.asie.charset.lib.item.ItemBase;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLockingDyeable extends ItemBase {
    @SideOnly(Side.CLIENT)
    public static class Color implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            IDyeableItem item = stack.getCapability(Capabilities.DYEABLE_ITEM, null);
            if (tintIndex > 0 && item.hasColor(0)) {
                return 0xFF000000 | item.getColor(0);
            }

            return CharsetStorageLocks.DEFAULT_LOCKING_COLOR;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        IDyeableItem item = stack.getCapability(Capabilities.DYEABLE_ITEM, null);
        if (item.hasColor(0)) {
            tooltip.add(LockEventHandler.getColorDyed(item.getColor(0)));
        }
    }

    @Override
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new DyeableItemStack(stack, 1, false);
    }
}
