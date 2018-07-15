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

package pl.asie.charset.module.misc.drama;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pl.asie.charset.lib.item.ItemBase;

public class ItemDramaInABottle extends ItemBase {
	public ItemDramaInABottle() {
		super();
		setTranslationKey("charset.dramaInABottle");
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("drama")) {
				player.sendMessage(new TextComponentString(stack.getTagCompound().getString("drama")));
			} else {
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setString("drama", DramaGenerator.INSTANCE.generateDrama());
				player.sendMessage(new TextComponentString(stack.getTagCompound().getString("drama")));
			}
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}
}