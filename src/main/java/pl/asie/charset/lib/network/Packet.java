package pl.asie.charset.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class Packet implements IMessage {
	public abstract void readData(ByteBuf buf);
	public abstract void writeData(ByteBuf buf);

	@Override
	public final void fromBytes(ByteBuf buf) {
		readData(buf);
	}

	@Override
	public final void toBytes(ByteBuf buf) {
		writeData(buf);
	}
}
