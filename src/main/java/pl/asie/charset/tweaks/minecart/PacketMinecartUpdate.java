package pl.asie.charset.tweaks.minecart;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;

import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.network.PacketEntity;
import pl.asie.charset.tweaks.ModCharsetTweaks;

public class PacketMinecartUpdate extends PacketEntity {
	public PacketMinecartUpdate() {
		super();
	}

	public PacketMinecartUpdate(Entity entity) {
		super(entity);
	}

	public static void send(EntityMinecart minecart) {
		ModCharsetTweaks.packet.sendToAllAround(new PacketMinecartUpdate(minecart), minecart, 128);
	}

	private static void update(EntityMinecart minecart, int color) {
		MinecartProperties properties = MinecartProperties.get(minecart);
		if (properties != null) {
			properties.setColor(color);
		}
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		final int color = buf.readInt();

		if (entity instanceof EntityMinecart) {
			final EntityMinecart minecart = (EntityMinecart) entity;
			if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
				Minecraft.getMinecraft().addScheduledTask(new Runnable() {
					@Override
					public void run() {
						update(minecart, color);
					}
				});
			} else {
				update(minecart, color);
			}
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);

		EntityMinecart minecart = (EntityMinecart) entity;
		MinecartProperties properties = MinecartProperties.get(minecart);
		buf.writeInt(properties != null ? properties.getColor() : -1);
	}
}
