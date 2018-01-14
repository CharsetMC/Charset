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

package pl.asie.charset.module.transport.dyeableMinecarts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.network.PacketEntity;

public class PacketMinecartRequest extends PacketEntity {
	public PacketMinecartRequest() {
		super();
	}

	public PacketMinecartRequest(Entity entity) {
		super(entity);
	}

	public static void send(EntityMinecart minecart) {
		CharsetTransportDyeableMinecarts.packet.sendToServer(new PacketMinecartRequest(minecart));
	}

	@Override
	public void apply(INetHandler handler) {
		super.apply(handler);
		if (entity instanceof EntityMinecart) {
			PacketMinecartUpdate.send((EntityMinecart) entity);
		}
	}

	@Override
	public boolean isAsynchronous() {
		return true;
	}
}
