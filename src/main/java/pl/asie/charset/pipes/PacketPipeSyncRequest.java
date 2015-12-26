package pl.asie.charset.pipes;

import io.netty.buffer.ByteBuf;

import mcmultipart.multipart.IMultipart;
import pl.asie.charset.lib.network.PacketPart;

/**
 * Created by asie on 12/2/15.
 */
public class PacketPipeSyncRequest extends PacketPart {
	public PacketPipeSyncRequest() {
		super();
	}

	public PacketPipeSyncRequest(IMultipart part) {
		super(part);
	}

	@Override
	public void readData(ByteBuf buf) {
		super.readData(buf);

		if (part == null || !(part instanceof PartPipe)) {
			return;
		}

		((PartPipe) part).onSyncRequest();
	}
}
