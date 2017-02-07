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

package pl.asie.charset.crafting.pocket;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetHandler;
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
	public void readData(INetHandler handler, ByteBuf buf) {
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
	public void writeData(ByteBuf buf) {
		buf.writeByte(action);
		buf.writeInt(arg);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
