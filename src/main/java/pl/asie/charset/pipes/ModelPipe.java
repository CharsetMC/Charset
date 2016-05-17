package pl.asie.charset.pipes;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.render.ModelPipeLike;
import pl.asie.charset.lib.utils.ClientUtils;

/**
 * Created by asie on 5/17/16.
 */
public class ModelPipe extends ModelPipeLike<PartPipe> {
    public static final ResourceLocation PIPE_TEXTURE_LOC = new ResourceLocation("charsetpipes:blocks/pipe");

    public ModelPipe() {
        super(PartPipe.PROPERTY);
    }

    @Override
    public float getThickness() {
        return 7.995f;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public TextureAtlasSprite getTexture(EnumFacing side, int connectionMatrix) {
        return ClientUtils.textureGetter.apply(PIPE_TEXTURE_LOC);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return ClientUtils.textureGetter.apply(PIPE_TEXTURE_LOC);
    }
}
