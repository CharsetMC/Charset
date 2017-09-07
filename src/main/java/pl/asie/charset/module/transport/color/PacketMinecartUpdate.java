/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.transport.color;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.lib.network.PacketEntity;

public class PacketMinecartUpdate extends PacketEntity {
	private EnumDyeColor color;

	public PacketMinecartUpdate() {
		super();
	}

	public PacketMinecartUpdate(Entity entity) {
		super(entity);
	}

	public static void send(EntityMinecart minecart) {
		CharsetTransportDyeableMinecarts.packet.sendToAllAround(new PacketMinecartUpdate(minecart), minecart, 128);
	}

	private static void update(EntityMinecart minecart, EnumDyeColor color) {
		MinecartDyeable properties = MinecartDyeable.get(minecart);
		if (properties != null) {
			properties.setColor(color);
		}
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		int intCol = buf.readInt();
		if (intCol < 0) color = null;
		else color = EnumDyeColor.byMetadata(intCol);
	}

	@Override
	public void apply(INetHandler handler) {
		if (entity instanceof EntityMinecart) {
			final EntityMinecart minecart = (EntityMinecart) entity;
			update(minecart, color);
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);

		EntityMinecart minecart = (EntityMinecart) entity;
		MinecartDyeable properties = MinecartDyeable.get(minecart);
		buf.writeInt(properties != null && properties.getColor() != null ? properties.getColor().getMetadata() : -1);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
