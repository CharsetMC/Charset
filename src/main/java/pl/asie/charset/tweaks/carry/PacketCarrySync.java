package pl.asie.charset.tweaks.carry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.Packet;

public class PacketCarrySync extends Packet {
	private Entity player;
	private NBTTagCompound tag;

	public PacketCarrySync() {

	}

	public PacketCarrySync(Entity player) {
		this.player = player;
		this.tag = (NBTTagCompound) CarryHandler.STORAGE.writeNBT(TweakCarry.CAPABILITY, player.getCapability(TweakCarry.CAPABILITY, null), null);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		int dimension = buf.readInt();
		World world = ModCharsetLib.proxy.getLocalWorld(dimension);
		player = world.getEntityByID(buf.readInt());
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void apply() {
		if (player != null && player.hasCapability(TweakCarry.CAPABILITY, null)) {
			CarryHandler carryHandler = player.getCapability(TweakCarry.CAPABILITY, null);
			carryHandler.setPlayer(player);
			CarryHandler.STORAGE.readNBT(TweakCarry.CAPABILITY, carryHandler, null, tag);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(player.getEntityWorld().provider.getDimension());
		buf.writeInt(player.getEntityId());
		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
