package pl.asie.charset.crafting;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.network.Packet;

public class PacketPTAction extends Packet {
	public static final int BALANCE = 0;
	public static final int SWIRL = 1;
	public static final int CLEAR = 2;
	public static final int FILL = 3;

	private EntityPlayer player;
	private int action;
	private int arg;

	public PacketPTAction() {

	}

	public PacketPTAction(int action) {
		this.action = action;
	}

	public PacketPTAction(int action, int arg) {
		this.action = action;
		this.arg = arg;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		player = getPlayer(handler);
		action = buf.readUnsignedByte();
		arg = buf.readInt();
	}

	@Override
	public void apply() {
		Container c = player.openContainer;
		if (c instanceof ContainerPocket) {
			((ContainerPocket) c).onAction(action, arg);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeByte(action);
		buf.writeInt(arg);
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
