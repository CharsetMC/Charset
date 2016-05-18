package pl.asie.charset.lib.render;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.utils.RenderUtils;

/**
 * Created by asie on 3/31/16.
 */
public class CharsetFaceBakery extends FaceBakery {
    public BakedQuad makeBakedQuad(Vector3f min, Vector3f max, int tintIndex,
                                   TextureAtlasSprite icon, EnumFacing facing, ModelRotation rot, boolean uvLocked) {
        return makeBakedQuad(min, max, tintIndex, RenderUtils.calculateUV(min, max, facing), icon, facing, rot, uvLocked);
    }

    public BakedQuad makeBakedQuad(Vector3f min, Vector3f max, int tintIndex, float[] uv,
                                   TextureAtlasSprite icon, EnumFacing facing, ModelRotation rot, boolean uvLocked) {
        BakedQuad quad = makeBakedQuad(
                min, max,
                new BlockPartFace(null, -1, "", new BlockFaceUV(uv, 0)),
                icon, facing, rot, null, uvLocked, true
        );

        if (tintIndex != -1) {
            RenderUtils.recolorQuad(quad, tintIndex);
        }

        return quad;
    }
}
