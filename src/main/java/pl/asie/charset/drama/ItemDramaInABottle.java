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

package pl.asie.charset.drama;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemDramaInABottle extends Item {
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!worldIn.isRemote) {
			if (itemStackIn.hasTagCompound() && itemStackIn.getTagCompound().hasKey("drama")) {
				playerIn.addChatComponentMessage(new TextComponentString(itemStackIn.getTagCompound().getString("drama")));
			} else {
				itemStackIn.setTagCompound(new NBTTagCompound());
				itemStackIn.getTagCompound().setString("drama", DramaGenerator.INSTANCE.createDrama());
				playerIn.addChatComponentMessage(new TextComponentString(itemStackIn.getTagCompound().getString("drama")));
			}
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStackIn);
	}
}