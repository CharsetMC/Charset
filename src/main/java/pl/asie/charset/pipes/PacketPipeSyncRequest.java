package pl.asie.charset.pipes;

import io.netty.buffer.ByteBuf;

import net.minecraft.tileentity.TileEntity;

import pl.asie.charset.lib.network.PacketTile;

/**
 * Created by asie on 12/2/15.
 */
public class PacketPipeSyncRequest extends PacketTile {
	public PacketPipeSyncRequest() {
		super();
	}

	public PacketPipeSyncRequest(TileEntity tile) {
		super(tile);
	}

	@Override
	public void readData(ByteBuf buf) {
		super.readData(buf);

		if (tile == null || !(tile instanceof TilePipe)) {
			return;
		}

		((TilePipe) tile).onSyncRequest();
	}
}
