/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.module.tools.building.trowel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.module.tools.building.ToolsUtils;

public class ItemTrowel extends ItemBase {
    public ItemTrowel() {
        super();
        setUnlocalizedName("charset.trowel");
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
