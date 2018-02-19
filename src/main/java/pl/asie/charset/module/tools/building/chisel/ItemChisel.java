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

package pl.asie.charset.module.tools.building.chisel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockStairs;
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
import net.minecraftforge.fml.common.Loader;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.material.FastRecipeLookup;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.RecipeUtils;
import pl.asie.charset.module.tools.building.ItemCharsetTool;
import pl.asie.charset.module.tools.building.ToolsUtils;

public class ItemChisel extends ItemCharsetTool {
    public ItemChisel() {
        super();
        if (Loader.isModLoaded("chiselsandbits")) {
            setUnlocalizedName("charset.chisel.large");
        } else {
            setUnlocalizedName("charset.chisel");
        }
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    int getBlockMask(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("mask", Constants.NBT.TAG_ANY_NUMERIC)) {
            return stack.getTagCompound().getInteger("mask");
        } else {
            return 0b000_011_011;
        }
    }

    void setBlockMask(ItemStack stack, int mask) {
        ItemUtils.getTagCompound(stack, true).setShort("mask", (short) mask);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            if (!world.isRemote) {
                player.openGui(ModCharset.instance, GuiHandlerCharset.CHISEL, player.getEntityWorld(),
                        player.inventory.currentItem, 0, 0);
            }

            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        } else {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand == EnumHand.OFF_HAND) {
            return EnumActionResult.PASS;
        }

        if (!worldIn.isRemote) {
            IBlockState state = worldIn.getBlockState(pos);
            if (!state.getBlock().isAir(state, worldIn, pos)) {
                ItemStack inputStack = state.getBlock().getPickBlock(state, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos), worldIn, pos, playerIn);
                if (!inputStack.isEmpty() && inputStack.getCount() == 1) {
                    ItemStack heldItem = playerIn.getHeldItem(EnumHand.MAIN_HAND);
                    ItemStack[] inputStacks = new ItemStack[9];
                    int inputMask = getBlockMask(heldItem);
                    int inputCount = 0;
                    for (int i = 0; i < 9; i++) {
                        if ((inputMask & (1 << i)) != 0) {
                            inputStacks[i] = inputStack;
                            inputCount++;
                        }
                    }

                    ItemStack result = FastRecipeLookup.getCraftingResult(worldIn, 3, 3, inputStacks);
                    if (result != null && !result.isEmpty() && !(result.getItem() instanceof ItemBlock && ((ItemBlock) result.getItem()).getBlock() instanceof BlockButton)) {
                        if (result.getItem() instanceof ItemBlock) {
                            Block block = ((ItemBlock) result.getItem()).getBlock();
                            if (block instanceof BlockButton) {
                                return EnumActionResult.FAIL;
                            }

                            if (block instanceof BlockStairs && (inputCount == 6 && result.getCount() >= 4 && result.getCount() <= 8)) {
                                result.setCount(inputCount);
                            }
                        }

                        if ((result.getCount() % inputCount) == 0) {
                            ItemStack resultCopy = result.copy();
                            resultCopy.setCount(1);
                            ActionResult<ItemStack> placeResult = ToolsUtils.placeBlockOrRollback(resultCopy, playerIn, worldIn, pos);
                            if (placeResult.getType() == EnumActionResult.FAIL) {
                                return EnumActionResult.FAIL;
                            }
                            if (result.getCount() > inputCount) {
                                ItemStack resultCopyRemainder = result.copy();
                                resultCopyRemainder.setCount((result.getCount() / inputCount) - 1);
                                Block.spawnAsEntity(worldIn, pos, resultCopyRemainder);
                            }
                        }
                    }
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
