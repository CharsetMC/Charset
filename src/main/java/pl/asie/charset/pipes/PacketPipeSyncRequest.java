package pl.asie.charset.pipes;

import io.netty.buffer.ByteBuf;

import net.minecraftforge.fmp.multipart.IMultipart;
import net.minecraft.network.INetHandler;
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
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);

		if (part == null || !(part instanceof PartPipe)) {
			return;
		}

		((PartPipe) part).onSyncRequest();
	}
}
