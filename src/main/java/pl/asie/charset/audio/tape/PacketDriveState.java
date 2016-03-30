package pl.asie.charset.audio.tape;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.INetHandler;
import net.minecraft.server.MinecraftServer;

import mcmultipart.multipart.IMultipart;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketPart;

public class PacketDriveState extends PacketPart {
	private State state;

	public PacketDriveState() {
		super();
	}

	public PacketDriveState(IMultipart tile, State state) {
		super(tile);
		this.state = state;
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		final State newState = State.values()[buf.readUnsignedByte()];

		if (part instanceof PartTapeDrive) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					((PartTapeDrive) part).setState(newState);
				}
			};

			if (part.getWorld().isRemote) {
				if (ModCharsetLib.proxy.isClientThread()) {
					runnable.run();
				} else {
					ModCharsetLib.proxy.addScheduledClientTask(runnable);
				}
			} else {
				if (!getThreadListener(handler).isCallingFromMinecraftThread()) {
					getThreadListener(handler).addScheduledTask(runnable);
				} else {
					runnable.run();
				}
			}
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		buf.writeByte(state.ordinal());
	}
}
