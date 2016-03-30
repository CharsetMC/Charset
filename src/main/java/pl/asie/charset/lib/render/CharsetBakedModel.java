package pl.asie.charset.lib.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.lib.utils.ClientUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asie on 3/30/16.
 */
public class CharsetBakedModel implements IBakedModel {
    private final List<BakedQuad>[] quads = new List[7];

    public CharsetBakedModel() {
        for (int i = 0; i < quads.length; i++) {
            quads[i] = new ArrayList<>();
        }
    }

    public void addQuad(EnumFacing side, BakedQuad quad) {
        quads[side == null ? 6 : side.ordinal()].add(quad);
    }

    public void addModel(IBakedModel model) {
        for (int i = 0; i < 7; i++) {
            quads[i].addAll(model.getQuads(null, i == 6 ? null : EnumFacing.getFront(i), 0));
        }
    }

    public void addModel(IBakedModel model, int tint) {
        for (int i = 0; i < 7; i++) {
            EnumFacing side = i == 6 ? null : EnumFacing.getFront(i);
            ClientUtils.addRecoloredQuads(model.getQuads(null, side, 0), tint, quads[i], side);
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return quads[side == null ? 6 : side.ordinal()];
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
