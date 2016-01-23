package pl.asie.charset.audio.tape;

import io.netty.buffer.ByteBuf;

import mcmultipart.multipart.IMultipart;
import pl.asie.charset.lib.network.PacketPart;

public class PacketDriveCounter extends PacketPart {
	private int counter;

	public PacketDriveCounter() {
		super();
	}

	public PacketDriveCounter(IMultipart tile, int counter) {
		super(tile);
		this.counter = counter;
	}

	@Override
	public void readData(ByteBuf buf) {
		super.readData(buf);
		counter = buf.readInt();

		if (part instanceof PartTapeDrive) {
			PartTapeDrive drive = (PartTapeDrive) part;
			drive.state.counter = counter;
			if (!drive.getWorld().isRemote) {
				drive.state.updateCounter();
			}
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		buf.writeInt(counter);
	}
}
