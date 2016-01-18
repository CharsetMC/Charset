package pl.asie.charset.lib.network;

import java.util.EnumMap;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;

import mcmultipart.multipart.IMultipart;

public class PacketRegistry {
	private EnumMap<Side, FMLEmbeddedChannel> channels;
	private TIntObjectMap<Class<? extends Packet>> idPacketMap = new TIntObjectHashMap<Class<? extends Packet>>();
	private TObjectIntMap<Class<? extends Packet>> packetIdMap = new TObjectIntHashMap<Class<? extends Packet>>();

	public PacketRegistry(String channelName) {
		channels = NetworkRegistry.INSTANCE.newChannel(channelName, new PacketChannelHandler(this));
	}

	public void registerPacket(int id, Class<? extends Packet> packet) {
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

	public void sendToWatching(Packet message, World world, BlockPos pos) {
		for (EntityPlayerMP player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			if (player.worldObj.provider.getDimensionId() == world.provider.getDimensionId()) {
				if (((WorldServer) player.worldObj).getPlayerManager().isPlayerWatchingChunk(player, pos.getX() >> 4, pos.getZ() >> 4)) {
					channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
					channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
					channels.get(Side.SERVER).writeOutbound(message);
				}
			}
		}
	}

	public void sendToWatching(Packet message, TileEntity tile) {
		sendToWatching(message, tile.getWorld(), tile.getPos());
	}

	public void sendTo(Packet message, EntityPlayerMP player) {
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

	public void sendToAllAround(Packet packet, IMultipart entity,
								double d) {
		this.sendToAllAround(packet, new TargetPoint(entity.getWorld().provider.getDimensionId(),
				entity.getPos().getX(), entity.getPos().getY(), entity.getPos().getZ(), d));
	}

	public void sendToAllAround(Packet packet, TileEntity entity,
								double d) {
		this.sendToAllAround(packet, new TargetPoint(entity.getWorld().provider.getDimensionId(),
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
