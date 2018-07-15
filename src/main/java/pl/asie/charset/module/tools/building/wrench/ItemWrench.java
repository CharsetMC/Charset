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
import pl.asie.charset.lib.utils.RayTraceUtils;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;
import pl.asie.charset.module.tools.building.ICustomRotateBlock;
import pl.asie.charset.module.tools.building.ItemCharsetTool;

import java.util.Optional;

public class ItemWrench extends ItemCharsetTool {
    public ItemWrench() {
        super();
        setTranslationKey("charset.wrench");
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
