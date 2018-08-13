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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.lib.notify.Notice;
import pl.asie.charset.lib.notify.NoticeStyle;
import pl.asie.charset.lib.notify.component.NotificationComponentString;
import pl.asie.charset.lib.utils.ItemUtils;

import java.util.UUID;

public class ItemStopwatch extends ItemBase {
	public ItemStopwatch() {
		super();
		setMaxStackSize(1);
		setTranslationKey("charset.stopwatch");
	}

	public String getKey(ItemStack stack) {
		NBTTagCompound tag = ItemUtils.getTagCompound(stack, true);
		if (!tag.hasKey("uid", Constants.NBT.TAG_STRING)) {
			String uid = UUID.randomUUID().toString();
			tag.setString("uid", uid);
			return uid;
		} else {
			return tag.getString("uid");
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		EnumActionResult result = EnumActionResult.SUCCESS;
		ItemStack stack = player.getHeldItem(hand);

		if (!world.isRemote) {
			if (world.hasCapability(CharsetToolsEngineering.stopwatchTrackerCap, null)) {
				StopwatchTracker tracker = world.getCapability(CharsetToolsEngineering.stopwatchTrackerCap, null);
				if (tracker.clearPosition(getKey(stack))) {
					player.sendStatusMessage(new TextComponentTranslation("notice.charset.stopwatch.cleared"), true);
				}
			}
		}

		return new ActionResult<>(result, stack);
	}


	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && world.hasCapability(CharsetToolsEngineering.stopwatchTrackerCap, null)) {
			ItemStack stack = player.getHeldItem(hand);
			StopwatchTracker tracker = world.getCapability(CharsetToolsEngineering.stopwatchTrackerCap, null);
			//noinspection ConstantConditions
			StopwatchTracker.AddPositionResult result = tracker.addPosition(getKey(stack), pos);
			new Notice(pos, NotificationComponentString.translated(result == StopwatchTracker.AddPositionResult.END ? "notice.charset.stopwatch.markEnd" : "notice.charset.stopwatch.markStart"))
					.sendTo(player);
		}

		return EnumActionResult.SUCCESS;
	}
}
