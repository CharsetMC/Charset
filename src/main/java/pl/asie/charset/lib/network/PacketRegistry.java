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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import pl.asie.charset.ModCharset;

import java.util.EnumMap;
import java.util.HashSet;

public class PacketRegistry {
	private static final HashSet<String> usedChannelNames = new HashSet<>();

	private EnumMap<Side, FMLEmbeddedChannel> channels;
	private TIntObjectMap<Class<? extends Packet>> idPacketMap = new TIntObjectHashMap<Class<? extends Packet>>();
	private TObjectIntMap<Class<? extends Packet>> packetIdMap = new TObjectIntHashMap<Class<? extends Packet>>();

	public PacketRegistry(String channelName) {
		if (channelName.length() > 20) {
			String trunc = channelName.substring(0, 20);
			ModCharset.logger.warn("Channel name too long: " + channelName + ", truncating to " + trunc);
			channelName = trunc;
		}
		if (usedChannelNames.contains(channelName)) {
			throw new RuntimeException("Channel name already used: " + channelName);
		}

		channels = NetworkRegistry.INSTANCE.newChannel(channelName, new PacketChannelHandler(this));
		usedChannelNames.add(channelName);
	}

	public void registerPacket(int id, Class<? extends Packet> packet) {
		try {
			packet.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("No empty constructor defined! This is a Charset bug!", e);
		}
		idPacketMap.put(id, packet);
		packetIdMap.put(packet, id);
	}

	public net.minecraft.network.Packet getPacketFrom(Packet message) {
		return channels.get(Side.SERVER).generatePacketFrom(message);
	}

	public void sendToAll(Packet message) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channels.get(Side.SERVER).writeOutbound(message);
	}

	public void sendToWatching(Packet message, World world, BlockPos pos, Entity except) {
		WorldServer worldServer = (WorldServer) world;
		for (EntityPlayerMP player : worldServer.getMinecraftServer().getPlayerList().getPlayers()) {
			if (player != except && player.world.provider.getDimension() == world.provider.getDimension()) {
				if (worldServer.getPlayerChunkMap().isPlayerWatchingChunk(player, pos.getX() >> 4, pos.getZ() >> 4)) {
					channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
					channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
					channels.get(Side.SERVER).writeOutbound(message);
				}
			}
		}
	}

	public void sendToWatching(Packet message, TileEntity tile) {
		sendToWatching(message, tile.getWorld(), tile.getPos(), null);
	}

	public void sendToWatching(Packet message, Entity entity) {
		sendToWatching(message, entity.getEntityWorld(), entity.getPosition(), null);
	}

	// TODO: Slightly ugly.
	public void sendToWatching(Packet message, TileEntity tile, Entity except) {
		sendToWatching(message, tile.getWorld(), tile.getPos(), except);
	}

	public void sendToWatching(Packet message, Entity entity, Entity except) {
		sendToWatching(message, entity.getEntityWorld(), entity.getPosition(), except);
	}

	public void sendTo(Packet message, EntityPlayer player) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channels.get(Side.SERVER).writeOutbound(message);
	}

	public void sendToAllAround(Packet message, NetworkRegistry.TargetPoint point) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
		channels.get(Side.SERVER).writeOutbound(message);
	}

	public void sendToDimension(Packet message, int dimensionId) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
		channels.get(Side.SERVER).writeOutbound(message);
	}

	public void sendToServer(Packet message) {
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeOutbound(message);
	}

	public void sendToAllAround(Packet packet, TileEntity entity,
								double d) {
		this.sendToAllAround(packet, new TargetPoint(entity.getWorld().provider.getDimension(),
				entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ(), d));
	}

	public void sendToAllAround(Packet packet, Entity entity,
								double d) {
		this.sendToAllAround(packet, new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, d));
	}

	public Packet instantiatePacket(int i) {
		try {
			return idPacketMap.get(i).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getPacketId(Class<? extends Packet> aClass) {
		return packetIdMap.get(aClass);
	}
}
