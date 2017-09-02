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
