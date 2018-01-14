/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.render.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class SimpleBakedModel implements IBakedModel {
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
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        Pair<? extends IBakedModel, Matrix4f> pair = parent.handlePerspective(cameraTransformType);
        if (pair.getLeft() != parent) {
            return pair;
        } else {
            return ImmutablePair.of(this, pair.getRight());
        }
    }
}
