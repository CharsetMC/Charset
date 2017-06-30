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
		setUnlocalizedName("charset.dramaInABottle");
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