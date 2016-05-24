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

package pl.asie.charset.wrench;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.RayTraceUtils;

public class ItemWrench extends Item {
    public ItemWrench() {
        super();
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.wrench");
        setHarvestLevel("wrench", 2);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        EnumFacing targetFacing = facing != null ? facing : EnumFacing.UP;
        if (playerIn != null && playerIn.isSneaking()) {
            targetFacing = targetFacing.getOpposite();
        }

        IBlockState state = worldIn.getBlockState(pos);
        if (state != null) {
            Vec3d start = RayTraceUtils.getStart(playerIn);
            Vec3d end = RayTraceUtils.getEnd(playerIn);
            RayTraceResult hit = state.collisionRayTrace(worldIn, pos, start, end);
            if (state.getBlock().canPlayerRotate(worldIn, pos, targetFacing, playerIn, hit)) {
                if (!worldIn.isRemote) {
                    state.getBlock().rotateBlock(worldIn, pos, targetFacing, playerIn, hit);
                }
                return EnumActionResult.SUCCESS;
            } else {
                return EnumActionResult.PASS;
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
