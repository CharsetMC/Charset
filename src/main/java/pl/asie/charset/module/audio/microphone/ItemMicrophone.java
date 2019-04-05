/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.audio.microphone;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.UUID;

public class ItemMicrophone extends ItemBase {
	public ItemMicrophone() {
		super();
		setTranslationKey("charset.audio_microphone");
		setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile == null || !tile.hasCapability(CharsetAudioMicrophone.WIRELESS_AUDIO_RECEIVER, null)) {
			if (!worldIn.isRemote) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "DECIDEDLY NOT OWO >:("));
			}
			return EnumActionResult.FAIL;
		}

		if (!worldIn.isRemote) {
			NBTTagCompound tag = ItemUtils.getTagCompound(player.getHeldItem(hand), true);
			tag.setInteger("rW", worldIn.provider.getDimension());
			tag.setInteger("rX", pos.getX());
			tag.setInteger("rY", pos.getY());
			tag.setInteger("rZ", pos.getZ());

			player.sendMessage(new TextComponentString("OwO"));
		}
		return EnumActionResult.SUCCESS;
	}

	public boolean hasReceiver(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("rW", Constants.NBT.TAG_ANY_NUMERIC);
	}

	public int getReceiverDimension(ItemStack stack) {
		NBTTagCompound tag = ItemUtils.getTagCompound(stack, true);
		return tag.getInteger("rW");
	}

	public BlockPos getReceiverPos(ItemStack stack) {
		NBTTagCompound tag = ItemUtils.getTagCompound(stack, true);
		return new BlockPos(tag.getInteger("rX"), tag.getInteger("rY"), tag.getInteger("rZ"));
	}

	public UUID getId(ItemStack stack) {
		NBTTagCompound tag = ItemUtils.getTagCompound(stack, true);
		if (!tag.hasUniqueId("senderId")) {
			tag.setUniqueId("senderId", UUID.randomUUID());
		}
		return tag.getUniqueId("senderId");
	}
}
