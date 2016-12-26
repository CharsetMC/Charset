/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class SimpleBakedModel implements IPerspectiveAwareModel {
    private final List<BakedQuad>[] quads = new List[7];
    private final IBakedModel parent;
    private TextureAtlasSprite particle;

    public SimpleBakedModel() {
        this(null);
    }

    public SimpleBakedModel(IBakedModel parent) {
        this.parent = parent;
        for (int i = 0; i < quads.length; i++) {
            quads[i] = new ArrayList<>();
        }
    }

    public void setParticle(TextureAtlasSprite particle) {
        this.particle = particle;
    }

    public void addQuad(EnumFacing side, BakedQuad quad) {
        quads[side == null ? 6 : side.ordinal()].add(quad);
    }

    public void addModel(IBakedModel model) {
        for (int i = 0; i < 7; i++) {
            quads[i].addAll(model.getQuads(null, i == 6 ? null : EnumFacing.getFront(i), 0));
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return quads[side == null ? 6 : side.ordinal()];
    }

    @Override
    public boolean isAmbientOcclusion() {
        return parent != null ? parent.isAmbientOcclusion() : true;
    }

    @Override
    public boolean isGui3d() {
        return parent != null ? parent.isGui3d() : true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        if (particle != null) {
            return particle;
        } else {
            return parent != null ? parent.getParticleTexture() : null;
        }
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        if (parent != null && parent instanceof IPerspectiveAwareModel) {
            Pair<? extends IBakedModel, Matrix4f> pair = ((IPerspectiveAwareModel) parent).handlePerspective(cameraTransformType);
            if (pair.getLeft() != parent) {
                return pair;
            } else {
                return ImmutablePair.of(this, pair.getRight());
            }
        } else {
            return ImmutablePair.of(this, null);
        }
    }
}
