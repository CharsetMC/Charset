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
 *
 * Copyright (c) 2014 copygirl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pl.asie.charset.tweaks;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

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
		if (!(event.getEntity() instanceof EntityPlayerMP) || event.getEntity().world.isRemote || event.getOriginal().isEmpty()) {
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
		InventoryPlayer inv = player.inventory;

		ItemStack currentItem = inv.getCurrentItem();
		if (currentItem != event.getOriginal()) {
			return;
		}

		int row;

		for (row = 2; row >= 0; row--) {
			int slot = inv.currentItem + row * 9 + 9;
			ItemStack stackAbove = inv.getStackInSlot(slot);
			if (!canReplace(stackAbove, event.getOriginal())) break;
			int targetSlot = ((slot < 27) ? (slot + 9) : (slot - 27));
			inv.setInventorySlotContents(targetSlot, stackAbove.copy());
			inv.setInventorySlotContents(slot, ItemStack.EMPTY);
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
		if (classesSrc.size() > 0 && classesSrc.equals(classesDst)) {
			return true;
		}

		if (destroyed.getItem() instanceof ItemSword && replacement.getItem() instanceof ItemSword) {
			return true;
		}

		// Generic fallback check
		return ItemUtils.equals(replacement, destroyed, false, !destroyed.getItem().isDamageable(), true);
	}
}
