package pl.asie.charset.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.INetHandler;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class Packet {
	protected IThreadListener getThreadListener(INetHandler handler) {
		return FMLCommonHandler.instance().getWorldThread(handler);
	}

	public abstract void readData(INetHandler handler, ByteBuf buf);

	public abstract void writeData(ByteBuf buf);
}
