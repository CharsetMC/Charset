package pl.asie.charset.audio.tape;

import io.netty.buffer.ByteBuf;

import mcmultipart.multipart.IMultipart;
import net.minecraft.network.INetHandler;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.audio.ProxyClient;
import pl.asie.charset.lib.network.PacketPart;

public class PacketDriveStop extends PacketPart {
	public PacketDriveStop() {
		super();
	}

	public PacketDriveStop(IMultipart tile) {
		super(tile);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);

		if (part instanceof IAudioSource) {
			ProxyClient.stream.remove((IAudioSource) part);
		}
	}
}
