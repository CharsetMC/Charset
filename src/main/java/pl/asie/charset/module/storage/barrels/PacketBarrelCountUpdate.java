package pl.asie.charset.module.storage.barrels;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
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
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeInt(count);
	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		count = buf.readInt();
	}

	@Override
	public void apply(INetHandler handler) {
		if (tile != null && tile instanceof TileEntityDayBarrel) {
			((TileEntityDayBarrel) tile).onCountUpdate(this);
		}
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
