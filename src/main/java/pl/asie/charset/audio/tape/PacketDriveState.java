package pl.asie.charset.audio.tape;

import io.netty.buffer.ByteBuf;

import net.minecraft.tileentity.TileEntity;

import pl.asie.charset.lib.network.PacketTile;

public class PacketDriveState extends PacketTile {
	private State state;

	public PacketDriveState() {
		super();
	}

	public PacketDriveState(TileEntity tile, State state) {
		super(tile);
		this.state = state;
	}

	@Override
	public void readData(ByteBuf buf) {
		super.readData(buf);
		state = State.values()[buf.readUnsignedByte()];

		if (tile instanceof TileTapeDrive) {
			((TileTapeDrive) tile).setState(state);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		buf.writeByte(state.ordinal());
	}
}
