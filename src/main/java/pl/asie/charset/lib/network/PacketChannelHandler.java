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

import java.util.List;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

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
			newMsg.readData(iNetHandler, msg.payload());
		}
	}
}
