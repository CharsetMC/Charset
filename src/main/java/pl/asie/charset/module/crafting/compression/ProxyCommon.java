package pl.asie.charset.module.crafting.compression;

import net.minecraft.tileentity.TileEntity;

public class ProxyCommon {
	public void init() {
	}

	public void markShapeRender(TileCompressionCrafter sender, CompressionShape shape) {
		CharsetCraftingCompression.packet.sendToAllAround(new PacketCompactAnimation(sender), sender, 144.0D);
	}
}
