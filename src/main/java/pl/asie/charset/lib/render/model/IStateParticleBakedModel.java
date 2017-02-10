package pl.asie.charset.lib.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface IStateParticleBakedModel {
	TextureAtlasSprite getParticleTexture(IBlockState state);
}
