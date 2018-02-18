package pl.asie.charset.module.crafting.compression;

import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import pl.asie.charset.lib.network.PacketTile;

public class PacketCompactAnimation extends PacketTile {
	private long craftingTickStart, craftingTickEnd;

	public PacketCompactAnimation(TileCompressionCrafter tile) {
		super(tile);
		craftingTickStart = tile.shape.craftingTickStart;
		craftingTickEnd = tile.shape.craftingTickEnd;
	}

	public PacketCompactAnimation() {

	}

	@Override
	public void readData(INetHandler handler, PacketBuffer buf) {
		super.readData(handler, buf);
		craftingTickStart = buf.readLong();
		craftingTickEnd = craftingTickStart + buf.readVarInt();
	}

	@Override
	public void apply(INetHandler handler) {
		super.apply(handler);
		if (tile instanceof TileCompressionCrafter) {
			TileCompressionCrafter crafter = (TileCompressionCrafter) tile;
			crafter.getShape(false);
			if (crafter.shape != null) {
				crafter.shape.craftingTickStart = craftingTickStart;
				crafter.shape.craftingTickEnd = craftingTickEnd;
				CharsetCraftingCompression.proxy.markShapeRender(crafter, crafter.shape);
			}
		}
	}

	@Override
	public void writeData(PacketBuffer buf) {
		super.writeData(buf);
		buf.writeLong(craftingTickStart);
		buf.writeVarInt((int) (craftingTickEnd - craftingTickStart));
	}

	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
