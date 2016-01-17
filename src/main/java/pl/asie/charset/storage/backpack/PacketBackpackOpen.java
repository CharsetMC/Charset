package pl.asie.charset.storage.backpack;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;

import pl.asie.charset.lib.network.PacketEntity;
import pl.asie.charset.storage.ModCharsetStorage;

/**
 * Created by asie on 1/12/16.
 */
public class PacketBackpackOpen extends PacketEntity {
	public PacketBackpackOpen() {

	}

	public PacketBackpackOpen(EntityPlayer player) {
		super(player);
	}

	@Override
	public void readData(ByteBuf buf) {
		super.readData(buf);
		if (entity instanceof EntityPlayer) {
			((EntityPlayer) entity).openGui(ModCharsetStorage.instance, 2, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ);
		}
	}
}
