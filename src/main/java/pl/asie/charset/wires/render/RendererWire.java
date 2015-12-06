package pl.asie.charset.wires.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;

public class RendererWire extends RendererWireBase {
	private final List<RendererWireBase> renderers = new ArrayList<RendererWireBase>();

	public RendererWire() {
		renderers.add(new RendererWireNormal("wire", 2));
	}

	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		return renderers.get(0).handleBlockState(state);
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing face) {
		return null;
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		return null;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return renderers.get(0).getParticleTexture();
	}

	@Override
	public void loadTextures(TextureMap map) {
		for (RendererWireBase renderer : renderers) {
			renderer.loadTextures(map);
		}
	}
}
