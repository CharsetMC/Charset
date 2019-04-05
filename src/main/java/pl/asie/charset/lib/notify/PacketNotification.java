/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.notify.component.NotificationComponent;
import pl.asie.charset.lib.notify.component.NotificationComponentString;
import pl.asie.charset.lib.notify.component.NotificationComponentUnknown;
import pl.asie.charset.lib.notify.component.NotificationComponentUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;

public class PacketNotification extends Packet {
	enum Type {
		COORD,
		VEC3,
		ENTITY,
		TILEENTITY,
		ONSCREEN;

		static Type[] VALUES = values();
	}

	private Type type;
	private BlockPos pos;
	private Object target;
	private Collection<NoticeStyle> style;
	private NotificationComponent msg;

	private EntityPlayer me;

	public PacketNotification() {

	}

	public static PacketNotification createOnscreen(Collection<NoticeStyle> style, NotificationComponent message) {
		PacketNotification n = new PacketNotification();
		n.type = Type.ONSCREEN;
		n.style = style;
		n.msg = message;
		return n;
	}

	public static PacketNotification createNotify(Object where, Collection<NoticeStyle> style, NotificationComponent message) {
		PacketNotification n = new PacketNotification();

		if (where instanceof NotificationCoord) {
			where = ((NotificationCoord) where).getPos();
		}

		n.target = where;

		if (where instanceof Vec3d) {
			n.type = Type.VEC3;
		} else if (where instanceof BlockPos) {
			n.type = Type.COORD;
		} else if (where instanceof Entity) {
			n.type = Type.ENTITY;
		} else if (where instanceof TileEntity) {
			n.type = Type.TILEENTITY;
		} else {
			return null;
		}

		n.style = style;
		n.msg = message;
		return n;
	}

	private void writeStyles(PacketBuffer output) {
		output.writeByte(style.size());
		for (NoticeStyle s : style) {
			output.writeByte(s.ordinal());
		}
	}

	private void readStyles(PacketBuffer input) {
		style = EnumSet.noneOf(NoticeStyle.class);
		int size = input.readUnsignedByte();
		for (int i = 0; i < size; i++) {
			style.add(NoticeStyle.values()[input.readUnsignedByte()]);
		}
	}

	private void readMsg(PacketBuffer input) {
		try {
			msg = NotificationComponentUtil.deserialize(input);
		} catch (Exception e) {
			e.printStackTrace();
			msg = NotificationComponentString.raw("#ERR");
		}
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer input) {
		me = getPlayer(handler);
		type = Type.VALUES[input.readByte()];

		switch (type) {
			case COORD:
				pos = new BlockPos(input.readInt(), input.readInt(), input.readInt());
				target = new NotificationCoord(me.world, pos);
				break;
			case ENTITY:
				int id = input.readInt();
				if (id == me.getEntityId()) {
					target = me; //bebna
				} else {
					target = me.world.getEntityByID(id);
				}
				break;
			case TILEENTITY:
				pos = new BlockPos(input.readInt(), input.readInt(), input.readInt());
				target = me.world.getTileEntity(pos);
				if (target == null) {
					target = new NotificationCoord(me.world, pos);
				}
				break;
			case VEC3:
				target = new Vec3d(input.readDouble(), input.readDouble(), input.readDouble());
				break;
			case ONSCREEN:
				readStyles(input);
				readMsg(input);
				return;
			default: return;
		}

		if (target == null) {
			return;
		}

		readStyles(input);
		readMsg(input);
	}

	@Override
	public void apply(INetHandler handler) {
		switch (type) {
			case ONSCREEN:
				NotifyImplementation.proxy.onscreen(style, msg);
				break;
			default:
				NotifyImplementation.recieve(me, target, style, msg);
				break;
		}
	}

	@Override
	public void writeData(PacketBuffer output) {
		output.writeByte(type.ordinal());

		switch (type) {
			case COORD:
				BlockPos pos = (BlockPos) target;
				output.writeInt(pos.getX());
				output.writeInt(pos.getY());
				output.writeInt(pos.getZ());
				break;
			case VEC3:
				Vec3d v = (Vec3d) target;
				output.writeDouble(v.x);
				output.writeDouble(v.y);
				output.writeDouble(v.z);
				break;
			case ENTITY:
				Entity ent = (Entity) target;
				output.writeInt(ent.getEntityId());
				break;
			case TILEENTITY:
				TileEntity te = (TileEntity) target;
				output.writeInt(te.getPos().getX());
				output.writeInt(te.getPos().getY());
				output.writeInt(te.getPos().getZ());
				break;
			case ONSCREEN:
				writeStyles(output);
				NotificationComponentUtil.serialize(msg, output);
				return;
		}

		writeStyles(output);
		NotificationComponentUtil.serialize(msg, output);
	}

	@Override
	public boolean isAsynchronous() {
		return true;
	}
}
