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
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class NotificationComponentString extends NotificationComponent {
	private final boolean format;
	private final String string;
	private final NotificationComponent[] components;

	protected NotificationComponentString(boolean format, String s, NotificationComponent... components) {
		this.format = format;
		this.string = s;
		this.components = components;
	}

	public static NotificationComponentString translated(String s, NotificationComponent... components) {
		return new NotificationComponentString(true, s, components);
	}

	public static NotificationComponentString raw(String s, NotificationComponent... components) {
		return new NotificationComponentString(false, s, components);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof NotificationComponentString)) {
			return false;
		} else {
			NotificationComponentString ncs = (NotificationComponentString) other;
			if (this.format != ncs.format || !this.string.equals(ncs.string)) {
				return false;
			}

			if (this.components.length != ncs.components.length) {
				return false;
			}

			for (int i = 0; i < components.length; i++) {
				if (!this.components[i].equals(ncs.components[i])) {
					return false;
				}
			}

			return true;
		}
	}

	@Override
	public String toString() {
		if (format) {
			String[] strings = new String[components.length];
			for (int i = 0; i < components.length; i++) {
				strings[i] = components[i].toString();
			}

			return I18n.translateToLocalFormatted(string, (Object[]) strings);
		} else {
			return String.format(string, (Object[]) components);
		}
	}

	public static class Factory implements NotificationComponentFactory<NotificationComponentString> {
		@Override
		public Class<NotificationComponentString> getComponentClass() {
			return NotificationComponentString.class;
		}

		@Override
		public void serialize(NotificationComponentString component, PacketBuffer buffer) {
			buffer.writeBoolean(component.format);
			buffer.writeString(component.string);
			buffer.writeVarInt(component.components.length);
			for (NotificationComponent c : component.components) {
				NotificationComponentUtil.serialize(c, buffer);
			}
		}

		@Override
		public NotificationComponentString deserialize(PacketBuffer buffer) {
			boolean f = buffer.readBoolean();
			String s = buffer.readString(32767);
			int l = buffer.readVarInt();
			NotificationComponent[] cps = new NotificationComponent[l];
			for (int i = 0; i < l; i++) {
				cps[i] = NotificationComponentUtil.deserialize(buffer);
			}
			return new NotificationComponentString(f, s, cps);
		}
	}
}
