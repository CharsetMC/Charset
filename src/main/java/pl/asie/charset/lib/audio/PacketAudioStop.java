package pl.asie.charset.lib.audio;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;
import pl.asie.charset.lib.network.Packet;

public class PacketAudioStop extends Packet {
	public PacketAudioStop() {
		super();
	}
	private int id;

	protected PacketAudioStop(int id) {
		super();
		this.id = id;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		AudioStreamManager.INSTANCE.remove(buf.readInt());
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(id);
	}
}
