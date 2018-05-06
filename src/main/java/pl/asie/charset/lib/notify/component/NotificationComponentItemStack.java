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

package pl.asie.charset.lib.notify.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import pl.asie.charset.lib.utils.ItemUtils;
import pl.asie.charset.lib.utils.ThreeState;
import pl.asie.charset.lib.utils.UtilProxyCommon;

import java.io.IOException;
import java.util.ArrayList;

public class NotificationComponentItemStack extends NotificationComponent {
	private final ItemStack stack;
	private final boolean showName, showInfo;

	public NotificationComponentItemStack(ItemStack stack, boolean showName, boolean showInfo) {
		this.stack = stack;
		this.showName = showName;
		this.showInfo = showInfo;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof NotificationComponentItemStack)) {
			return false;
		} else {
			NotificationComponentItemStack ncs = (NotificationComponentItemStack) other;
			if (this.showName != ncs.showName || this.showInfo != ncs.showInfo) {
				return false;
			}

			return ItemStack.areItemStacksEqual(this.stack, ncs.stack);
		}
	}

	@Override
	public String toString() {
		if (!stack.isEmpty()) {
			StringBuilder builder = new StringBuilder();

			if (showName) {
				builder.append(stack.getDisplayName());
			}

			if (showName && showInfo) {
				builder.append('\n');
			}

			if (showInfo) {
				ArrayList<String> bits = new ArrayList<>();
				UtilProxyCommon.proxy.addInformation(stack, UtilProxyCommon.proxy.getLocalPlayer().world, bits, ThreeState.NO);
				boolean tail = false;
				for (String s : bits) {
					if (tail) {
						builder.append('\n');
					} else {
						tail = true;
					}
					builder.append(s);
				}
			}

			return builder.toString();
		} else {
			return "null";
		}
	}

	public static class Factory implements NotificationComponentFactory<NotificationComponentItemStack> {
		@Override
		public Class<NotificationComponentItemStack> getComponentClass() {
			return NotificationComponentItemStack.class;
		}

		@Override
		public void serialize(NotificationComponentItemStack component, PacketBuffer buffer) {
			buffer.writeBoolean(component.showName);
			buffer.writeBoolean(component.showInfo);
			buffer.writeItemStack(component.stack);
		}

		@Override
		public NotificationComponentItemStack deserialize(PacketBuffer buffer) {
			boolean showName = buffer.readBoolean();
			boolean showInfo = buffer.readBoolean();
			try {
				ItemStack stack = buffer.readItemStack();
				return new NotificationComponentItemStack(stack, showName, showInfo);
			} catch (IOException e) {
				return new NotificationComponentItemStack(ItemStack.EMPTY, showName, showInfo);
			}
		}
	}
}
