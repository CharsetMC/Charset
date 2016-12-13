package pl.asie.charset.storage.barrel;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import pl.asie.charset.lib.network.PacketTile;

public class PacketBarrelCountUpdate extends PacketTile {
	protected int count;

	public PacketBarrelCountUpdate() {
		super();
	}

	public PacketBarrelCountUpdate(TileEntityDayBarrel tile) {
		super(tile);
		count = tile.getItemCount();
	}

	@Override
	public void writeData(ByteBuf buf) {
		super.writeData(buf);
		buf.writeMedium(count);
	}

	@Override
	public void readData(INetHandler handler, ByteBuf buf) {
		super.readData(handler, buf);
		count = buf.readMedium();
	}

	@Override
	public void apply() {
		if (tile != null && tile instanceof TileEntityDayBarrel) {
			((TileEntityDayBarrel) tile).onCountUpdate(this);
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
