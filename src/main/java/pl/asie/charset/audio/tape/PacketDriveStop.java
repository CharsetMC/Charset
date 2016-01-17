package pl.asie.charset.audio.tape;

import io.netty.buffer.ByteBuf;

import net.minecraft.tileentity.TileEntity;

import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.audio.ProxyClient;
import pl.asie.charset.lib.network.PacketTile;

public class PacketDriveStop extends PacketTile {
	public PacketDriveStop() {
		super();
	}

	public PacketDriveStop(TileEntity tile) {
		super(tile);
	}

	@Override
	public void readData(ByteBuf buf) {
		super.readData(buf);

		if (tile instanceof IAudioSource) {
			ProxyClient.stream.remove((IAudioSource) tile);
		}
	}
}
