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

package pl.asie.charset.lib.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.util.List;

@Sharable
public class PacketChannelHandler extends MessageToMessageCodec<FMLProxyPacket, Packet> {
	private final PacketRegistry registry;

	public PacketChannelHandler(PacketRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg,
						  List<Object> out) throws Exception {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeByte(registry.getPacketId(msg.getClass()));
		msg.writeData(buffer);
		FMLProxyPacket proxy = new FMLProxyPacket(buffer, ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
		out.add(proxy);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg,
						  List<Object> out) throws Exception {
		INetHandler iNetHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
		Packet newMsg = registry.instantiatePacket(msg.payload().readUnsignedByte());
		if (newMsg != null) {
			newMsg.readData(iNetHandler, new PacketBuffer(msg.payload()));
			if (newMsg.isAsynchronous()) {
				newMsg.apply(iNetHandler);
			} else {
				IThreadListener listener = Packet.getThreadListener(iNetHandler);

				if (listener.isCallingFromMinecraftThread()) {
					newMsg.apply(iNetHandler);
				} else {
					listener.addScheduledTask(() -> newMsg.apply(iNetHandler));
				}
			}
		}
	}
}
