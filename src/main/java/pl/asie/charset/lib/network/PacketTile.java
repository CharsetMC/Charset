package pl.asie.charset.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import pl.asie.charset.lib.ModCharsetLib;

public abstract class PacketTile extends Packet {
	protected TileEntity tile;

	public PacketTile() {

	}

	public PacketTile(TileEntity tile) {
		this.tile = tile;
	}

	@Override
	public void readData(ByteBuf buf) {
		int dim = buf.readInt();
		int x = buf.readInt();
		int y = buf.readUnsignedShort();
		int z = buf.readInt();

		World w = ModCharsetLib.proxy.getLocalWorld(dim);

		if (w != null) {
			tile = w.getTileEntity(x, y, z);
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(tile.getWorldObj().provider.dimensionId);
		buf.writeInt(tile.xCoord);
		buf.writeShort(tile.yCoord);
		buf.writeInt(tile.zCoord);
	}
}
