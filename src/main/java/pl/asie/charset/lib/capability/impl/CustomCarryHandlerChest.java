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

package pl.asie.charset.lib.capability.impl;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.carry.CustomCarryHandler;
import pl.asie.charset.api.carry.ICarryHandler;

import java.util.List;

public class CustomCarryHandlerChest extends CustomCarryHandler {
    public CustomCarryHandlerChest(ICarryHandler handler) {
        super(handler);
    }

    @Override
    public void onPlace(World world, BlockPos pos) {
        super.onPlace(world, pos);

        for (EnumFacing facing1 : EnumFacing.HORIZONTALS) {
            if (world.getBlockState(pos.offset(facing1)).getBlock() instanceof BlockChest) {
                // FIXME: Double chests need this (#137)
                IBlockState state = owner.getState();
                List<ItemStack> drops = state.getBlock().getDrops(world, pos, state, 0);

                owner.getState().getBlock().onBlockPlacedBy(world, pos, owner.getState(), (EntityLivingBase) owner.getCarrier(), drops.size() > 0 ? drops.get(0) : ItemStack.EMPTY);
                break;
            }
        }
    }
}
