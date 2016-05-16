package pl.asie.charset.tweaks;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.lib.utils.ItemUtils;

/**
 * Ported from the Minecraft mod "copycore" by copygirl.
 * <p/>
 * Copyright (c) 2014 copygirl
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class TweakAutoReplace extends Tweak {
	public TweakAutoReplace() {
		super("tweaks", "autoReplace", "Automatically replaces broken items in the same column.", true);
	}

	@Override
	public void enable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void disable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
		if (!(event.getEntity() instanceof EntityPlayerMP) || event.getEntity().worldObj.isRemote) {
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
		InventoryPlayer inv = player.inventory;

		ItemStack currentItem = inv.getCurrentItem();
		if (currentItem != null && currentItem != event.getOriginal()) {
			return;
		}

		int row;

		for (row = 2; row >= 0; row--) {
			int slot = inv.currentItem + row * 9 + 9;
			ItemStack stackAbove = inv.getStackInSlot(slot);
			if (!canReplace(stackAbove, event.getOriginal())) break;
			int targetSlot = ((slot < 27) ? (slot + 9) : (slot - 27));
			inv.setInventorySlotContents(targetSlot, stackAbove);
			inv.setInventorySlotContents(slot, null);
			player.connection.sendPacket(
					new SPacketSetSlot(0, slot + 9, stackAbove));
		}

		if (row < 2) {
			player.connection.sendPacket(
					new SPacketSetSlot(0, inv.currentItem + row * 9 + 18, null));
		}
	}

	/**
	 * Returns if the destroyed item can be replaced with this item.
	 */
	private static boolean canReplace(ItemStack replacement, ItemStack destroyed) {
		// TODO: Allow tools of the same type to act as replacement?
		// Also, maybe allow the same item with different NBT data (enchantments)?
		return ItemUtils.equals(replacement, destroyed, false, !destroyed.getItem().isDamageable(), true);
	}
}
