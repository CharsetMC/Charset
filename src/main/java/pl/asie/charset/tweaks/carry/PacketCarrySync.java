package pl.asie.charset.tweaks.carry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.utils.Utils;

public class PacketCarrySync extends Packet {
	private Entity player;
	private NBTTagCompound tag;

	public PacketCarrySync() {

	}

	public PacketCarrySync(Entity player) {
		this.player = player;
		this.tag = (NBTTagCompound) CarryHandler.PROVIDER.getStorage().writeNBT(CharsetTweakCarry.CAPABILITY, player.getCapability(CharsetTweakCarry.CAPABILITY, null), null);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		int dimension = buf.readInt();
		World world = Utils.getLocalWorld(dimension);
		int playerId = buf.readInt();
		if (world != null) {
			player = world.getEntityByID(playerId);
		}
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void apply(INetHandler handler) {
		if (player == null) {
			player = getPlayer(handler);
		}
		if (player != null && player.hasCapability(CharsetTweakCarry.CAPABILITY, null)) {
			CarryHandler carryHandler = player.getCapability(CharsetTweakCarry.CAPABILITY, null);
			carryHandler.setPlayer(player);
			CarryHandler.PROVIDER.getStorage().readNBT(CharsetTweakCarry.CAPABILITY, carryHandler, null, tag);
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
