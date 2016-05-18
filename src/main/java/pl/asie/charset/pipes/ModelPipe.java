package pl.asie.charset.pipes;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.render.ModelPipeLike;

public class ModelPipe extends ModelPipeLike<PartPipe> {
    public static final ResourceLocation PIPE_TEXTURE_LOC = new ResourceLocation("charsetpipes", "blocks/pipe");
    public static TextureAtlasSprite[] sprites;

    public ModelPipe() {
        super(PartPipe.PROPERTY);
    }

    @Override
    public float getThickness() {
        return 7.995f;
    }

    @Override
    public int getInsideColor(EnumFacing facing) {
        return facing.getAxis() == EnumFacing.Axis.Y ? 0xFFA0A0A0 : 0xFFC8C8C8;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public TextureAtlasSprite getTexture(EnumFacing side, int connectionMatrix) {
        return sprites != null ? sprites[connectionMatrix] : null;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprites != null ? sprites[15] : null;
    }
}
