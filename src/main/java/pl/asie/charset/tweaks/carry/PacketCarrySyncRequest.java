package pl.asie.charset.tweaks.carry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.tweaks.ModCharsetTweaks;

public class PacketCarrySyncRequest extends Packet {
	private EntityPlayer player;

	public PacketCarrySyncRequest() {

	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		player = getPlayer(handler);
	}

	@Override
	public void apply() {
		if (player != null && player.hasCapability(TweakCarry.CAPABILITY, null)) {
			ModCharsetTweaks.packet.sendTo(new PacketCarrySync(player.getCapability(TweakCarry.CAPABILITY, null)), player);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
