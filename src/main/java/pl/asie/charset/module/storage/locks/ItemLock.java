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

package pl.asie.charset.module.storage.locks;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.item.ItemBase;

import java.util.List;

public class ItemLock extends ItemBase {
    @SideOnly(Side.CLIENT)
    public static class Color implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            if (stack.hasTagCompound()) {
                for (int i = tintIndex; i >= 0; i--) {
                    String key = "color" + i;
                    if (stack.getTagCompound().hasKey(key)) {
                        return stack.getTagCompound().getInteger(key);
                    }
                }
            }

            return CharsetStorageLocks.DEFAULT_LOCKING_COLOR;
        }
    }

    public ItemLock() {
        super();
        setUnlocalizedName("charset.lock");
    }

    public String getKey(ItemStack stack) {
        return "charset:key:" + getRawKey(stack);
    }

    public String getRawKey(ItemStack stack) {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey("key") ? stack.getTagCompound().getString("key") : "null";
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (getKey(stack) == null) {
            return EnumActionResult.FAIL;
        }

        BlockPos blockpos = pos.offset(facing);

        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && playerIn.canPlayerEdit(blockpos, facing, stack)) {
            EntityLock lockEntity = new EntityLock(worldIn, stack, blockpos, facing);

            if (lockEntity.onValidSurface()) {
                if (!worldIn.isRemote) {
                    lockEntity.playPlaceSound();
                    worldIn.spawnEntity(lockEntity);
                }

                stack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.FAIL;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        if (ItemKey.DEBUG_KEY_ID) {
            tooltip.add(getKey(stack));
        }
    }
}
