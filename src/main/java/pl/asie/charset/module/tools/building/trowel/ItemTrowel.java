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

package pl.asie.charset.module.tools.building.trowel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.module.tools.building.ItemCharsetTool;
import pl.asie.charset.module.tools.building.ToolsUtils;

public class ItemTrowel extends ItemCharsetTool {
    public ItemTrowel() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand == EnumHand.MAIN_HAND) {
            int offset = player.inventory.currentItem;

            for (int i = 2; i >= 0; i--) {
                int invPos = i*9 + offset;
                ItemStack stack = player.inventory.getStackInSlot(invPos);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                    ActionResult<ItemStack> result = ToolsUtils.placeBlockOrRollback(stack, player, worldIn, pos);
                    player.inventory.setInventorySlotContents(invPos, result.getResult());

                    if (result.getType() == EnumActionResult.SUCCESS) {
                        break;
                    }
                }
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.PASS;
        }
    }
}
