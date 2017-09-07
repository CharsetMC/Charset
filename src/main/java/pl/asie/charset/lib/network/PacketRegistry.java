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
