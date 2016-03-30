package pl.asie.charset.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.Entity;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;

import pl.asie.charset.lib.ModCharsetLib;

public abstract class PacketEntity extends Packet {
	protected Entity entity;

	public PacketEntity() {

	}

	public PacketEntity(Entity entity) {
		this.entity = entity;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		int dim = buf.readInt();
		int id = buf.readInt();

		World w = ModCharsetLib.proxy.getLocalWorld(dim);

		if (w != null) {
			entity = w.getEntityByID(id);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(entity.worldObj.provider.getDimension());
		buf.writeInt(entity.getEntityId());
	}
}
