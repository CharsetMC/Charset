package pl.asie.charset.module.tweaks.carry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import pl.asie.charset.lib.network.Packet;
import pl.asie.charset.lib.utils.Utils;

public class PacketCarrySync extends Packet {
	private Entity player;
	private NBTTagCompound tag;
	private boolean isSelf;
	private int playerId, dimension;

	public PacketCarrySync() {

	}

	public PacketCarrySync(Entity player, boolean isSelf) {
		this.player = player;
		this.isSelf = isSelf;
		this.tag = (NBTTagCompound) CarryHandler.PROVIDER.getStorage().writeNBT(CharsetTweakBlockCarrying.CAPABILITY, player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null), null);
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		isSelf = buf.readBoolean();
		if (!isSelf) {
			dimension = buf.readInt();
			playerId = buf.readInt();
		}
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void apply(INetHandler handler) {
		if (isSelf) {
			player = getPlayer(handler);
		} else {
			World world = Utils.getLocalWorld(dimension);
			if (world != null) {
				player = world.getEntityByID(playerId);
			}
		}
		if (player != null && player.hasCapability(CharsetTweakBlockCarrying.CAPABILITY, null)) {
			CarryHandler carryHandler = player.getCapability(CharsetTweakBlockCarrying.CAPABILITY, null);
			carryHandler.setPlayer(player);
			CarryHandler.PROVIDER.getStorage().readNBT(CharsetTweakBlockCarrying.CAPABILITY, carryHandler, null, tag);
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		buf.writeBoolean(isSelf);
		if (!isSelf) {
			buf.writeInt(player.getEntityWorld().provider.getDimension());
			buf.writeInt(player.getEntityId());
		}
		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
