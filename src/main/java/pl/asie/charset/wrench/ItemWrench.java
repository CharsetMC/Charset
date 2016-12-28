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

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
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

public class ItemWrench extends Item {
    public ItemWrench() {
        super();
        setCreativeTab(ModCharsetLib.CREATIVE_TAB);
        setUnlocalizedName("charset.wrench");
        setHarvestLevel("wrench", 2);
        setMaxStackSize(1);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    // TODO 1.11
    /* @Optional.Method(modid = "mcmultipart")
    public EnumActionResult tryRotateMultipart(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing targetFacing) {
        IMultipartContainer container = MultipartHelper.getPartContainer(worldIn, pos);
        if (container != null) {
            Vec3d start = RayTraceUtils.getStart(playerIn);
            Vec3d end = RayTraceUtils.getEnd(playerIn);
            double dist = Double.POSITIVE_INFINITY;
            RayTraceUtils.AdvancedRayTraceResultPart result = null;

            for (IMultipart p : container.getParts()) {
                RayTraceUtils.AdvancedRayTraceResultPart pResult = p.collisionRayTrace(start, end);
                if (pResult != null) {
                    double d = pResult.squareDistanceTo(start);
                    if (d <= dist) {
                        dist = d;
                        result = pResult;
                    }
                }
            }

            if (result != null && result.hit != null && result.hit.partHit != null) {
                return result.hit.partHit.rotatePart(targetFacing) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
            } else {
                return EnumActionResult.FAIL;
            }
        } else {
            return EnumActionResult.PASS;
        }
    } */

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            EnumFacing targetFacing = facing != null ? facing : EnumFacing.UP;
            if (playerIn != null && playerIn.isSneaking()) {
                targetFacing = targetFacing.getOpposite();
            }

            /* if (Loader.isModLoaded("mcmultipart")) {
                EnumActionResult result = tryRotateMultipart(playerIn, worldIn, pos, targetFacing);
                if (res2ult != EnumActionResult.PASS) {
                    return result;
                }
            } */

            IBlockState state = worldIn.getBlockState(pos);
            if (state != null) {
                return state.getBlock().rotateBlock(worldIn, pos, targetFacing) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
