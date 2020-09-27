/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.lib.notify;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.notify.component.NotificationComponentString;

public class PacketPoint extends Packet {
	enum Type {
		COORD,
		ENTITY;

		static Type[] VALUES = values();
	}

	private EntityPlayer player;
	private Type type;
	private BlockPos pos;
	private Entity entity;
	private String message;

	public PacketPoint() {

	}

	public static PacketPoint atCoord(BlockPos pos, String message) {
		PacketPoint packetPoint = new PacketPoint();
		packetPoint.type = Type.COORD;
		packetPoint.message = message;
		packetPoint.pos = pos;
		return packetPoint;
	}

	public static PacketPoint atEntity(Entity entity, String message) {
		PacketPoint packetPoint = new PacketPoint();
		packetPoint.type = Type.ENTITY;
		packetPoint.message = message;
		packetPoint.entity = entity;
		return packetPoint;
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		player = getPlayer(handler);
		type = Type.VALUES[buf.readByte()];
		message = buildMessage(player, buf);

		switch (type) {
			case COORD:
				pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
				break;
			case ENTITY:
				entity = player.world.getEntityByID(buf.readInt());
				break;
		}
	}

	@Override
	public void apply(INetHandler handler) {
		if (player == null)
			return;

		Notice notice = null;

		switch (type) {
			case COORD:
				notice = new Notice(new NotificationCoord(player.world, pos), NotificationComponentString.raw(message));
				break;
			case ENTITY:
				notice = new Notice(entity, NotificationComponentString.raw(message));
				break;
		}

		notice.withStyle(NoticeStyle.DRAWFAR, NoticeStyle.VERY_LONG, NoticeStyle.SCALE_SIZE, NoticeStyle.EXACTPOSITION);
		double maxDistSq = 256 * 256;
		for (EntityPlayer viewer : player.world.playerEntities) {
			if (player.getDistanceSq(viewer) >= maxDistSq) continue;
			notice.sendTo(viewer);
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeByte(type.ordinal());
		ByteBufUtils.writeUTF8String(buf, message);

		switch (type) {
			case COORD:
				buf.writeInt(pos.getX());
				buf.writeInt(pos.getY());
				buf.writeInt(pos.getZ());
				break;
			case ENTITY:
				buf.writeInt(entity.getEntityId());
				break;
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}

	private String buildMessage(EntityPlayer player, ByteBuf input) {
		String base = "<" + player.getName() + ">";
		String msg = ByteBufUtils.readUTF8String(input);
		if (msg == null || msg.length() == 0) {
			return base;
		}
		return base + "\n" + msg;
	}
}
