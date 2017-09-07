/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.misc.pocketcraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.ui.GuiHandlerCharset;

public class ItemPocketTable extends ItemBase {
    
    public ItemPocketTable() {
        super();
        setUnlocalizedName("charset.pocket_crafting_table");
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            if (!world.isRemote) {
                player.openGui(ModCharset.instance, GuiHandlerCharset.POCKET_TABLE, player.getEntityWorld(),
                        player.inventory.currentItem, 0, 0);
            }

            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        } else {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
    }
}
