/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tools.building.chisel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gnu.trove.map.TIntObjectMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.lib.render.model.BaseBakedModel;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.render.model.ModelKey;
import pl.asie.charset.module.tools.building.CharsetToolsBuilding;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ChiselBakedModel extends BaseBakedModel {
    private BakedQuad buildQuad(EnumFacing side, TextureAtlasSprite sprite,
                                float x, float y, float w, float h, float z, float colorMultiplier) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
        builder.setQuadOrientation(side);
        builder.setTexture(sprite);
        if (side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
            buildVertex(builder, side, x / 16.0f, y / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, (x+w) / 16.0f, y / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, (x+w) / 16.0f, (y+h) / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, x / 16.0f, (y+h) / 16.0f, z / 16.0f, sprite, colorMultiplier);
        } else {
            buildVertex(builder, side, x / 16.0f, y / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, x / 16.0f, (y+h) / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, (x+w) / 16.0f, (y+h) / 16.0f, z / 16.0f, sprite, colorMultiplier);
            buildVertex(builder, side, (x+w) / 16.0f, y / 16.0f, z / 16.0f, sprite, colorMultiplier);
        }
        return builder.build();
    }

    private void buildVertex(UnpackedBakedQuad.Builder builder, EnumFacing side, float x, float y, float z, TextureAtlasSprite sprite, float colorMultiplier) {
        VertexFormat format = builder.getVertexFormat();
        for(int e = 0; e < format.getElementCount(); e++) {
            VertexFormatElement element = format.getElement(e);
            switch (element.getUsage()) {
                case POSITION:
                    builder.put(e, x, (1-y), z, 1);
                    break;
                case COLOR:
                    builder.put(e, colorMultiplier, colorMultiplier, colorMultiplier, 1);
                    break;
                case UV:
                    builder.put(e, sprite.getInterpolatedU(x*16), sprite.getInterpolatedV(y*16), 0, 1);
                    break;
                case NORMAL:
                    builder.put(e, side.getXOffset(), side.getYOffset(), side.getZOffset(), 0);
                    break;
                default:
                    builder.put(e);
                    break;
            }
        }
    }

    private final Cache<Integer, IBakedModel> cache;
    private final TextureAtlasSprite sprite;
    private final IBakedModel parent;
    private final ItemOverrideList list;
    private final List<BakedQuad> quads;

    public ChiselBakedModel(IBakedModel parent) {
        this.parent = parent;
        this.sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("charset:items/chisel_pattern");
        this.cache = CacheBuilder.newBuilder().softValues().expireAfterAccess(1, TimeUnit.MINUTES).build();
        this.list = new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                int mask = CharsetToolsBuilding.chisel.getBlockMask(stack);
                if (ModelFactory.DISABLE_CACHE) {
                    return new ChiselBakedModel(parent, sprite, cache, mask);
                }

                IBakedModel model = cache.getIfPresent(mask);
                if (model == null) {
                    model = new ChiselBakedModel(parent, sprite, cache, mask);
                    cache.put(mask, model);
                }
                return model;
            }
        };
        this.quads = null;
    }

    public ChiselBakedModel(IBakedModel parent, TextureAtlasSprite sprite, Cache<Integer, IBakedModel> cache, int mask) {
        this.parent = parent;
        this.sprite = sprite;
        this.cache = cache;
        this.list = ItemOverrideList.NONE;

        List<BakedQuad> quads = parent.getQuads(null, null, 0);
        if (mask != 0x1FF) {
            ImmutableList.Builder<BakedQuad> quads2 = ImmutableList.builder();
            quads2.addAll(quads);
            int blockMask = mask;
            outer: for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if ((blockMask & (1 << (y*3+x))) == 0) {
                        int width = 1;
                        int height = 1;

                        blockMask |= (1 << (y*3+x));
                        for (int x0 = x + 1; x0 < 3; x0++) {
                            if ((blockMask & (1 << (y*3+x0))) == 0) {
                                blockMask |= (1 << (y*3+x0));
                                width++;
                            } else {
                                break;
                            }
                        }

                        for (int y0 = y + 1; y0 < 3; y0++) {
                            boolean match = true;

                            for (int x0 = x; x0 < x+width; x0++) {
                                if ((blockMask & (1 << (y0 * 3 + x0))) != 0) {
                                    match = false;
                                    break;
                                }
                            }

                            if (match) {
                                for (int x0 = x; x0 < x+width; x0++) {
                                    blockMask |= (1 << (y0*3+x0));
                                }
                                height++;
                            } else {
                                break;
                            }
                        }

                        quads2.add(buildQuad(EnumFacing.NORTH, sprite, 4+x, 9+y, width, height, 7.49f, 0.6f));
                        quads2.add(buildQuad(EnumFacing.SOUTH, sprite, 4+x, 9+y, width, height, 8.51f, 0.6f));
                        if (blockMask == 0x1FF) {
                            break outer;
                        }
                    }
                }
            }

            this.quads = quads2.build();
        } else {
            this.quads = quads;
        }
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        Pair<? extends IBakedModel, Matrix4f> parentPair = parent.handlePerspective(cameraTransformType);
        return ImmutablePair.of(this, parentPair.getRight());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return side == null ? quads : ImmutableList.of();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return parent.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return list;
    }
}
