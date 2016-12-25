package pl.asie.charset.lib.notify;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.Packet;

import java.io.IOException;

public class PacketPoint extends Packet {
	enum Type {
		COORD,
		ENTITY;

		static Type[] VALUES = values();
	}

	private EntityPlayer player;
	private Type type;
	private BlockPos pos;
	private Entity entity;
	private String message;

	public PacketPoint() {

	}

	public static PacketPoint atCoord(BlockPos pos, String message) {
		PacketPoint packetPoint = new PacketPoint();
		packetPoint.type = Type.COORD;
		packetPoint.message = message;
		packetPoint.pos = pos;
		return packetPoint;
	}

	public static PacketPoint atEntity(Entity entity, String message) {
		PacketPoint packetPoint = new PacketPoint();
		packetPoint.type = Type.ENTITY;
		packetPoint.message = message;
		packetPoint.entity = entity;
		return packetPoint;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		player = handler instanceof NetHandlerPlayServer ? ((NetHandlerPlayServer) handler).playerEntity : null;
		type = Type.VALUES[buf.readByte()];
		message = buildMessage(player, buf);

		switch (type) {
			case COORD:
				pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
				break;
			case ENTITY:
				entity = player.world.getEntityByID(buf.readInt());
				break;
		}
	}

	@Override
	public void apply() {
		if (player == null)
			return;

		Notice notice = null;

		switch (type) {
			case COORD:
				notice = new Notice(new NotificationCoord(player.world, pos), message);
				break;
			case ENTITY:
				notice = new Notice(entity, message);
				break;
		}

		notice.withStyle(NoticeStyle.DRAWFAR, NoticeStyle.VERY_LONG, NoticeStyle.SCALE_SIZE, NoticeStyle.EXACTPOSITION);
		double maxDist = 0xFF * 0xFF;
		for (EntityPlayer viewer : player.world.playerEntities) {
			if (player.getDistanceSqToEntity(viewer) > maxDist) continue;
			notice.sendTo(viewer);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeByte(type.ordinal());
		ByteBufUtils.writeUTF8String(buf, message);

		switch (type) {
			case COORD:
				buf.writeInt(pos.getX());
				buf.writeInt(pos.getY());
				buf.writeInt(pos.getZ());
				break;
			case ENTITY:
				buf.writeInt(entity.getEntityId());
				break;
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}

	private String buildMessage(EntityPlayer player, ByteBuf input) {
		String base = "<" + player.getName() + ">";
		String msg = ByteBufUtils.readUTF8String(input);
		if (msg == null || msg.length() == 0) {
			return base;
		}
		return base + "\n" + msg;
	}
}
