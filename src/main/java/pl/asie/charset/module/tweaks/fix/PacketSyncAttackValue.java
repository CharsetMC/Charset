package pl.asie.charset.module.tweaks.fix;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.network.PacketEntity;

public class PacketSyncAttackValue extends PacketEntity {
	private float attackedAtYaw;

	public PacketSyncAttackValue() {
		super();
	}

	public PacketSyncAttackValue(Entity entity) {
		super(entity);
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		buf.writeFloat(((EntityLivingBase) entity).attackedAtYaw);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		attackedAtYaw = buf.readFloat();
	}

	@Override
	public void apply(INetHandler handler) {
		((EntityLivingBase) entity).attackedAtYaw = attackedAtYaw;
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
