package pl.asie.charset.module.tweaks.carry;

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
	private int playerId, dimension;

	public PacketCarrySync() {

	}

	public PacketCarrySync(Entity player) {
		this.player = player;
		this.tag = (NBTTagCompound) CarryHandler.PROVIDER.getStorage().writeNBT(CharsetTweakBlockCarrying.CAPABILITY, player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null), null);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		dimension = buf.readInt();
		playerId = buf.readInt();
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void apply(INetHandler handler) {
		World world = Utils.getLocalWorld(dimension);
		if (world != null) {
			player = world.getEntityByID(playerId);
		}
		if (player == null) {
			player = getPlayer(handler);
			if (player.getEntityId() != playerId) {
				player = null;
			}
		}
		if (player != null && player.hasCapability(CharsetTweakBlockCarrying.CAPABILITY, null)) {
			CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
			carryHandler.setPlayer(player);
			CarryHandler.PROVIDER.getStorage().readNBT(CharsetTweakBlockCarrying.CAPABILITY, carryHandler, null, tag);
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
