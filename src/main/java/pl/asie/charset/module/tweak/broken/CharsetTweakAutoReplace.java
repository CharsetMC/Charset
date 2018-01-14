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

package pl.asie.charset.module.tweak.broken;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import java.util.Set;

/* @CharsetModule(
		name = "tweak.autoReplace",
		description = "Automatically replace items in vertical columns upon them breaking",
		isDevOnly = true
) */
// TODO: Fix me, please! (Might involve actually tracking the ItemStack in hand nowadays... :|)
public class CharsetTweakAutoReplace {
	@SubscribeEvent
	public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
		if (!(event.getEntity() instanceof EntityPlayerMP) || event.getEntity().world.isRemote || event.getOriginal().isEmpty()) {
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
		InventoryPlayer inv = player.inventory;

		ItemStack currentItem = inv.getCurrentItem();
		if (currentItem != event.getOriginal()) {
			return;
		}

		int row, lastEmptiedSlot = -1;
		boolean changed = false;

		for (row = 2; row >= 0; row--) {
			int slot = inv.currentItem + row * 9 + 9;
			ItemStack stackAbove = inv.getStackInSlot(slot);
			if (!canReplace(stackAbove, event.getOriginal())) break;
			int targetSlot = ((slot < 27) ? (slot + 9) : (slot - 27));
			ItemStack stackTarget = stackAbove.copy();
			inv.setInventorySlotContents(targetSlot, stackTarget);
			inv.setInventorySlotContents(slot, ItemStack.EMPTY);
			player.connection.sendPacket(new SPacketSetSlot(-1, targetSlot, stackTarget));
			changed = true;
			lastEmptiedSlot = slot;
		}

		if (changed) {
			player.connection.sendPacket(new SPacketSetSlot(-1, lastEmptiedSlot, ItemStack.EMPTY));
		}

		inv.markDirty();
	}

	/**
	 * Returns if the destroyed item can be replaced with this item.
	 */
	private static boolean canReplace(@Nonnull ItemStack replacement, @Nonnull ItemStack destroyed) {
		if (replacement.isEmpty()) {
			return false;
		}

		// Check if same tool classes
		Set<String> classesSrc = destroyed.getItem().getToolClasses(destroyed);
		Set<String> classesDst = replacement.getItem().getToolClasses(replacement);
		if (classesSrc.size() > 0 || classesDst.size() > 0) {
			return classesSrc.equals(classesDst);
		}

		if (destroyed.getItem() instanceof ItemSword && replacement.getItem() instanceof ItemSword) {
			return true;
		}

		// Generic fallback check
		// TODO: Special NBT handling?
		return ItemUtils.equals(replacement, destroyed, false, !destroyed.getItem().isDamageable(), false);
	}
}
