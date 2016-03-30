package pl.asie.charset.tweaks.minecart;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;

import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.network.PacketEntity;
import pl.asie.charset.tweaks.ModCharsetTweaks;

public class PacketMinecartRequest extends PacketEntity {
	public PacketMinecartRequest() {
		super();
	}

	public PacketMinecartRequest(Entity entity) {
		super(entity);
	}

	public static void send(EntityMinecart minecart) {
		ModCharsetTweaks.packet.sendToServer(new PacketMinecartRequest(minecart));
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);

		if (entity instanceof EntityMinecart) {
			PacketMinecartUpdate.send((EntityMinecart) entity);
		}
	}
}
