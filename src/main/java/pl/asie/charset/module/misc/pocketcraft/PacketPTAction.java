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

package pl.asie.charset.module.misc.pocketcraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.lib.network.Packet;

public class PacketPTAction extends Packet {
	public static final int BALANCE = 0;
	public static final int SWIRL = 1;
	public static final int CLEAR = 2;
	public static final int FILL = 3;

	private int action;
	private int arg;

	public PacketPTAction() {

	}

	public PacketPTAction(int action) {
		this.action = action;
	}

	public PacketPTAction(int action, int arg) {
		this.action = action;
		this.arg = arg;
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		action = buf.readUnsignedByte();
		arg = buf.readInt();
	}

	@Override
	public void apply(INetHandler handler) {
		EntityPlayer player = getPlayer(handler);
		Container c = player.openContainer;
		if (c instanceof ContainerPocketTable) {
			((ContainerPocketTable) c).onAction(action, arg);
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeByte(action);
		buf.writeInt(arg);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
