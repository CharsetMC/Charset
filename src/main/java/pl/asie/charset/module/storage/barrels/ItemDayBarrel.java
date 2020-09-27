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

package pl.asie.charset.module.storage.barrels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.block.BlockBase;
import pl.asie.charset.lib.item.ItemBlockBase;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ItemDayBarrel extends ItemBlockBase {
    public ItemDayBarrel(BlockBase block) {
        super(block);
        setTranslationKey("charset.barrel");
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("SilkItem")) {
            return 1;
        } else {
            return super.getItemStackLimit(stack);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack is) {
        Set<BarrelUpgrade> upgradeSet = EnumSet.noneOf(BarrelUpgrade.class);
        if (is.hasTagCompound()) {
            TileEntityDayBarrel.populateUpgrades(upgradeSet, is.getTagCompound());
        }

        String lookup = "tile.charset.barrel.format";
        if (!upgradeSet.isEmpty()) {
            lookup = "tile.charset.barrel.format2";
        }

        StringBuilder type = new StringBuilder();
        for (BarrelUpgrade upgrade : upgradeSet) {
            if (type.length() > 0) {
                type.append(" ");
            }
            type.append(I18n.translateToLocal("tile.charset.barrel." + upgrade + ".name"));
        }

        return I18n.translateToLocalFormatted(lookup, type.toString(), TileEntityDayBarrel.getLog(is.getTagCompound()).getStack().getDisplayName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        addExtraInformation(stack, world, tooltip, advanced);
    }

    @SideOnly(Side.CLIENT) // Invokes a client-only function getTooltip
    protected void addExtraInformation(ItemStack is, World world, List<String> list, ITooltipFlag verbose) {
        Set<BarrelUpgrade> upgradeSet = EnumSet.noneOf(BarrelUpgrade.class);
        if (is.hasTagCompound()) {
            TileEntityDayBarrel.populateUpgrades(upgradeSet, is.getTagCompound());
        }

        if (upgradeSet.contains(BarrelUpgrade.SILKY)) {
            list.add(I18n.translateToLocal("tile.charset.barrel.SILKY.silkhint"));
            TileEntityDayBarrel db = new TileEntityDayBarrel();
            db.loadFromStack(is);
            int count = db.getItemCount();
            ItemStack item = db.getItemUnsafe();
            if (count > 0 && !item.isEmpty()) {
                if (item.getItem() == this) {
                    list.add("?");
                    return;
                }
                List<String> sub = item.getTooltip/* Client-only */(Minecraft.getMinecraft().player, ITooltipFlag.TooltipFlags.NORMAL /* Propagating verbose would be natural, but let's keep the tool-tip short */);
                item.getItem().addInformation(item, world, sub, verbose);
                if (!sub.isEmpty()) {
                    Object first = sub.get(0);
                    sub.set(0, count + " " + first);
                    list.addAll(sub);
                }
            }
        }
    }

    @Override
    public int getItemBurnTime(ItemStack stack) {
        if (stack.hasTagCompound()) {
            ItemStack burnStack = TileEntityDayBarrel.getLog(stack.getTagCompound()).getStack();
            return TileEntityFurnace.getItemBurnTime(burnStack);
        }

        return -1;
    }
}
