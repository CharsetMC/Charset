package pl.asie.charset.pipes;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import mcmultipart.client.multipart.MultipartRegistryClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.render.ModelFactory;

public class ProxyClient extends ProxyCommon {
	private SpecialRendererPipe rendererPipe;

	@Override
	public void registerRenderers() {
		MultipartRegistryClient.bindMultipartSpecialRenderer(PartPipe.class, rendererPipe = new SpecialRendererPipe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileShifter.class, new SpecialRendererShifter());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		rendererPipe.clearCache();
	}

}
