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

package pl.asie.charset.module.tools.building;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ToolsUtils {
    private ToolsUtils() {

    }

    public static boolean placeBlock(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos) {
        ItemStack heldItem = playerIn.getHeldItem(EnumHand.MAIN_HAND);

        playerIn.setHeldItem(EnumHand.MAIN_HAND, stack);
        EnumActionResult result1 = stack.onItemUse(
                playerIn, worldIn, pos, EnumHand.MAIN_HAND, EnumFacing.UP,
                0, 0, 0
        );
        playerIn.setHeldItem(EnumHand.MAIN_HAND, heldItem);

        if (result1 == EnumActionResult.SUCCESS && !worldIn.isAirBlock(pos)) {
            return true;
        } else {
            return false;
        }
    }

    public static ActionResult<ItemStack> placeBlockOrRollback(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos) {
        ItemStack oldStack = stack.copy();
        ItemStack heldItem = playerIn.getHeldItem(EnumHand.MAIN_HAND);

        // Take a snapshot
        IBlockState state = worldIn.getBlockState(pos);
        NBTTagCompound nbtTile = null;
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity != null) {
                nbtTile = tileEntity.writeToNBT(new NBTTagCompound());
            }
        }

        worldIn.setBlockToAir(pos);


        playerIn.setHeldItem(EnumHand.MAIN_HAND, stack);
        EnumActionResult result1 = stack.onItemUse(
                playerIn, worldIn, pos, EnumHand.MAIN_HAND, EnumFacing.UP,
                0, 0, 0
        );

        ItemStack placedItem = playerIn.getHeldItem(EnumHand.MAIN_HAND);
        playerIn.setHeldItem(EnumHand.MAIN_HAND, heldItem);

        if (result1 == EnumActionResult.SUCCESS && !worldIn.isAirBlock(pos)) {
            // Hooray!
            return new ActionResult<>(EnumActionResult.SUCCESS, placedItem);
        } else {
            // Rollback...
            worldIn.setBlockState(pos, state);
            if (nbtTile != null) {
                worldIn.setTileEntity(pos, TileEntity.create(worldIn, nbtTile));
            }
            return new ActionResult<>(EnumActionResult.FAIL, oldStack);
        }
    }
}
