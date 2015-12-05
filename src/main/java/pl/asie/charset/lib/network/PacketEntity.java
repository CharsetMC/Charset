package pl.asie.charset.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.Entity;
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
	public void readData(ByteBuf buf) {
		int dim = buf.readInt();
		int id = buf.readInt();

		World w = ModCharsetLib.proxy.getLocalWorld(dim);

		if (w != null) {
			entity = w.getEntityByID(id);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(entity.worldObj.provider.getDimensionId());
		buf.writeInt(entity.getEntityId());
	}
}
