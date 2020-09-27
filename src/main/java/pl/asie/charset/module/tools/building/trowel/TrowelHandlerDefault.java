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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import pl.asie.charset.module.tools.building.ToolsUtils;

public final class TrowelHandlerDefault implements TrowelHandler {
	public static TrowelHandler INSTANCE = new TrowelHandlerDefault();

	private TrowelHandlerDefault() {

	}

	@Override
	public boolean matches(EntityLivingBase player, EnumHand itemHand) {
		return player instanceof EntityPlayer;
	}

	@Override
	public boolean apply(EntityLivingBase player, EnumHand itemHand, BlockPos pos) {
		ItemStack stack = player.getHeldItem(itemHand);
		return player instanceof EntityPlayer && ToolsUtils.placeBlock(stack, (EntityPlayer) player, player.getEntityWorld(), pos);
	}
}
