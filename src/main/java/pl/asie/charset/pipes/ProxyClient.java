package pl.asie.charset.pipes;

import net.minecraftforge.fml.client.registry.ClientRegistry;

import pl.asie.charset.pipes.client.RendererPipeTile;
import pl.asie.charset.pipes.client.RendererShifterTile;

public class ProxyClient extends ProxyCommon {
	@Override
	public void registerRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TilePipe.class, new RendererPipeTile());
		ClientRegistry.bindTileEntitySpecialRenderer(TileShifter.class, new RendererShifterTile());
	}
}
