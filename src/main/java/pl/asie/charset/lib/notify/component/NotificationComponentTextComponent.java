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

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;

public class NotificationComponentTextComponent extends NotificationComponent {
	private final ITextComponent component;

	public NotificationComponentTextComponent(ITextComponent component) {
		this.component = component;
	}

	@Override
	public String toString() {
		return component.getFormattedText();
	}

	public static class Factory implements NotificationComponentFactory<NotificationComponentTextComponent> {
		@Override
		public Class<NotificationComponentTextComponent> getComponentClass() {
			return NotificationComponentTextComponent.class;
		}

		@Override
		public void serialize(NotificationComponentTextComponent component, PacketBuffer buffer) {
			buffer.writeTextComponent(component.component);
		}

		@Override
		public NotificationComponentTextComponent deserialize(PacketBuffer buffer) {
			try {
				return new NotificationComponentTextComponent(buffer.readTextComponent());
			} catch (Exception e) {
				return new NotificationComponentTextComponent(new TextComponentString("#ERR"));
			}
		}
	}
}
