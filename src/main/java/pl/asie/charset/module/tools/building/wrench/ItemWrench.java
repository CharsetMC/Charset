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

package pl.asie.charset.module.tools.building.wrench;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.MultipartHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.utils.RayTraceUtils;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;
import pl.asie.charset.module.tools.building.ItemCharsetTool;

import java.util.Optional;

public class ItemWrench extends ItemCharsetTool {
    public ItemWrench() {
        super();
        setUnlocalizedName("charset.wrench");
        setHarvestLevel("wrench", 2);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = "mcmultipart")
    public EnumActionResult tryRotateMultipart(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing targetFacing) {
        Optional<IMultipartContainer> uio = MultipartHelper.getContainer(worldIn, pos);
        if (uio.isPresent()) {
            IMultipartContainer ui = uio.get();
            Vec3d start = RayTraceUtils.getStart(playerIn);
            Vec3d end = RayTraceUtils.getEnd(playerIn);
            double dist = Double.POSITIVE_INFINITY;
            IPartInfo part = null;

            for (IPartInfo p : ui.getParts().values()) {
                RayTraceResult pResult = p.getPart().collisionRayTrace(p, start, end);
                if (pResult != null && pResult.hitVec != null) {
                    double d = pResult.hitVec.squareDistanceTo(start);
                    if (d <= dist) {
                        dist = d;
                        part = p;
                    }
                }
            }

            if (part != null) {
                // TODO return ... ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
                return EnumActionResult.FAIL;
            } else {
                return EnumActionResult.FAIL;
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            EnumFacing targetFacing = facing != null ? facing : EnumFacing.UP;
            if (playerIn != null && playerIn.isSneaking()) {
                targetFacing = targetFacing.getOpposite();
            }

            if (Loader.isModLoaded("mcmultipart")) {
                EnumActionResult result = tryRotateMultipart(playerIn, worldIn, pos, targetFacing);
                if (result != EnumActionResult.PASS) {
                    return result;
                }
            }

            IBlockState state = worldIn.getBlockState(pos);
            if (!state.getBlock().isAir(state, worldIn, pos)) {
                ICustomRotateBlock customRotateBlock = CharsetToolsBuilding.getRotationHandler(state.getBlock());
                if (customRotateBlock != null) {
                    return customRotateBlock.rotateBlock(worldIn, pos, state, targetFacing.getOpposite()) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
                } else {
                    return state.getBlock().rotateBlock(worldIn, pos, targetFacing.getOpposite()) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
