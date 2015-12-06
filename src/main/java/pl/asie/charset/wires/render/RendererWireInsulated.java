package pl.asie.charset.wires.render;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class RendererWireInsulated extends RendererWireNormal {
	public RendererWireInsulated(String type, int width, int height) {
		super(type, width, height);
	}

	@Override
	protected void configureRenderer(boolean isTop, int cmc) {
		faceBakery.uvScale = isTop ? 4 : 1;
		faceBakery.uvOffset = cmc;
	}

	public void loadTextures(TextureMap map) {
		icons[0] = icons[2] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_cross"));
		icons[1] = icons[3] = map.registerSprite(new ResourceLocation("charsetwires:blocks/" + type + "_full"));
	}
}
