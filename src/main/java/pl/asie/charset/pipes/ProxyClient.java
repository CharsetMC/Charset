package pl.asie.charset.pipes;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

import pl.asie.charset.lib.render.RendererPipeLikeBlock;
import pl.asie.charset.pipes.client.RendererPipeTile;
import pl.asie.charset.pipes.client.RendererShifterBlock;
import pl.asie.charset.pipes.client.RendererShifterTile;

public class ProxyClient extends ProxyCommon {
	public static RendererPipeLikeBlock pipeRender;
	public static RendererShifterBlock shifterRender;

	public void registerRenderers() {
		RenderingRegistry.registerBlockHandler(pipeRender = new RendererPipeLikeBlock() {
			@Override
			public float getPipeThickness() {
				return 0.5F;
			}
		});
		RenderingRegistry.registerBlockHandler(shifterRender = new RendererShifterBlock());

		ClientRegistry.bindTileEntitySpecialRenderer(TilePipe.class, new RendererPipeTile());
		ClientRegistry.bindTileEntitySpecialRenderer(TileShifter.class, new RendererShifterTile());
	}
}
