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

package pl.asie.charset.lib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pl.asie.charset.lib.utils.PacketUtils;
import pl.asie.charset.lib.utils.Utils;

public abstract class Packet {
	protected static final IThreadListener getThreadListener(INetHandler handler) {
		IThreadListener listener = FMLCommonHandler.instance().getWorldThread(handler);
		return listener == null ? Utils.getThreadListener() : listener;
	}

	protected static final EntityPlayer getPlayer(INetHandler handler) {
		return PacketUtils.getPlayer(handler);
	}

	public abstract void readData(INetHandler handler, ByteBuf buf);

	public abstract void apply(INetHandler handler);

	public abstract void writeData(ByteBuf buf);

	public abstract boolean isAsynchronous();
}
