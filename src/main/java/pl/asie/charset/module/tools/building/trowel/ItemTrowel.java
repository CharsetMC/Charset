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

package pl.asie.charset.module.tools.building.trowel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.module.tools.building.ItemCharsetTool;
import pl.asie.charset.module.tools.building.ToolsUtils;

import java.util.Optional;

public class ItemTrowel extends ItemCharsetTool {
    public ItemTrowel() {
        super();
        setMaxStackSize(1);
        setTranslationKey("charset.trowel");
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        // are we repeating ourselves?
        ItemStack stack = player.getHeldItem(EnumHand.OFF_HAND);
        TrowelCache cache = null;

        if (player.hasCapability(TrowelEventHandler.CACHE, null)) {
            cache = player.getCapability(TrowelEventHandler.CACHE, null);
        }

        // can we handle this?
        Optional<TrowelHandler> handler = TrowelEventHandler.getHandler(player, EnumHand.OFF_HAND);
        if (!handler.isPresent()) {
            return true;
        }

        // create snapshot
        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(player.getEntityWorld(), pos);
        ItemStack stackSnapshot = stack.copy();

        // remove block, collect drops
        NonNullList<ItemStack> drops = NonNullList.create();
        IBlockState state = player.getEntityWorld().getBlockState(pos);
        state.getBlock().getDrops(drops, player.getEntityWorld(), pos, state, 0);
        player.getEntityWorld().setBlockToAir(pos);

        if (!handler.get().apply(player, EnumHand.OFF_HAND, pos)) {
            snapshot.restore();
        }

        // add drops
        for (ItemStack dropStack : drops) {
            ItemUtils.spawnItemEntity(player.getEntityWorld(),
                    new Vec3d(pos).add(0.5, 0.5, 0.5),
                    dropStack,
                    0,0,0,0
            );
        }

        // mark cache
        if (cache != null) {
            cache.set(player.getEntityWorld(), stackSnapshot, pos);
        }

        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        return false;
    }
}
