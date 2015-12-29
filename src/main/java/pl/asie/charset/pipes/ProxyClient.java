package pl.asie.charset.pipes;

import net.minecraftforge.fml.client.registry.ClientRegistry;

import mcmultipart.client.multipart.MultipartRegistryClient;
import pl.asie.charset.pipes.client.SpecialRendererPipe;
import pl.asie.charset.pipes.client.SpecialRendererShifter;

public class ProxyClient extends ProxyCommon {
	@Override
	public void registerRenderers() {
        MultipartRegistryClient.bindMultipartSpecialRenderer(PartPipe.class, new SpecialRendererPipe());
		ClientRegistry.bindTileEntitySpecialRenderer(TileShifter.class, new SpecialRendererShifter());
    }
}
