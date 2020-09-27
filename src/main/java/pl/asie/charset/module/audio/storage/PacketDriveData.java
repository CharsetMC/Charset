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

package pl.asie.charset.module.audio.storage;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;

import pl.asie.charset.lib.network.Packet;

public class PacketDriveData extends Packet {
	private byte[] data;
	private int totalLength;
	private boolean isLast;

	public PacketDriveData() {
		super();
	}

	public PacketDriveData(byte[] data, int totalLength, boolean isLast) {
		super();
		this.totalLength = totalLength;
		this.isLast = isLast;
		this.data = data;
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		totalLength = buf.readInt();
		isLast = buf.readBoolean();
		int len = buf.readShort();
		if (len > 0) {
			data = new byte[len];
			buf.readBytes(data);
		}
	}

	@Override
	public void apply(INetHandler handler) {
		EntityPlayer player = getPlayer(handler);
		if (player != null && player.openContainer instanceof ContainerRecordPlayer) {
			TileRecordPlayer owner = ((ContainerRecordPlayer) player.openContainer).owner;
			owner.writeData(data, isLast, totalLength);
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeInt(totalLength);
		buf.writeBoolean(isLast);
		buf.writeShort(data.length);
		buf.writeBytes(data);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
