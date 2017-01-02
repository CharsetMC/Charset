package pl.asie.charset.tweaks.carry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.network.Packet;

public class PacketCarrySync extends Packet {
	private EntityPlayer player;
	private NBTTagCompound tag;

	public PacketCarrySync() {

	}

	public PacketCarrySync(CarryHandler handler) {
		tag = (NBTTagCompound) CarryHandler.STORAGE.writeNBT(TweakCarry.CAPABILITY, handler, null);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		player = getPlayer(handler);
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
		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
