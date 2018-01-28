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

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.IDyeableItem;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.List;

public class ItemLockingDyeable extends ItemBase implements IDyeableItem {
    @Override
    public int getColor(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("color") ? stack.getTagCompound().getInteger("color") : -1;
    }

    @Override
    public boolean setColor(ItemStack stack, int color) {
        ItemUtils.getTagCompound(stack, true).setInteger("color", color);
        return true;
    }

    @Override
    public boolean removeColor(ItemStack stack) {
        if (stack.hasTagCompound()) {
            stack.getTagCompound().removeTag("color");
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    public static class Color implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            if (tintIndex > 0 && stack.hasTagCompound() && stack.getTagCompound().hasKey("color")) {
                return stack.getTagCompound().getInteger("color");
            }

            return CharsetStorageLocks.DEFAULT_LOCKING_COLOR;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (hasColor(stack)) {
            tooltip.add(LockEventHandler.getColorDyed(getColor(stack)));
        }
    }
}
