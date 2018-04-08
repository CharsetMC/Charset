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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.misc.IConnectable;
import pl.asie.charset.lib.render.CharsetFaceBakery;
import pl.asie.charset.lib.utils.RenderUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ModelPipeShaped<T extends IConnectable> extends BaseBakedModel {
    private static final boolean RENDER_INNER_FACES = true;
    private static final boolean RENDER_OUTER_FACES = true;

    private static final EnumFacing[][] CONNECTION_DIRS = new EnumFacing[][]{
            {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH},
            {EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}
    };

    private static final ModelRotation[] ROTATIONS = new ModelRotation[]{
            ModelRotation.X0_Y0,
            ModelRotation.X180_Y0,
            ModelRotation.X270_Y0,
            ModelRotation.X270_Y180,
            ModelRotation.X270_Y270,
            ModelRotation.X270_Y90
    };

    private final List<BakedQuad>[] lists = new List[257];
    private final IUnlistedProperty<T> property;
    private final Block block;

    public ModelPipeShaped(IUnlistedProperty<T> property, Block block) {
        this.property = property;
        this.block = block;
        addDefaultBlockTransforms();
    }

    private List<BakedQuad> getPipeQuads(int i, BlockRenderLayer layer) {
        if (layer == null) {
            if (ModelFactory.DISABLE_CACHE || lists[256] == null) {
                boolean[] connections = new boolean[6];
                lists[256] = new ArrayList<>();

                for (BlockRenderLayer layer1 : BlockRenderLayer.values()) {
                    if (block.canRenderInLayer(block.getDefaultState(), layer1)) {
                        lists[256].addAll(generateQuads(connections, layer1));
                    }
                }
            }

            return lists[256];
        } else {
            i += 64 * layer.ordinal();
        }

        if (ModelFactory.DISABLE_CACHE || lists[i] == null) {
            if (block.canRenderInLayer(block.getDefaultState(), layer)) {
                boolean[] connections = new boolean[6];
                for (int j = 0; j < 6; j++) {
                    if ((i & (1 << j)) != 0) {
                        connections[5 - j] = true;
                    }
                }

                lists[i] = ImmutableList.copyOf(generateQuads(connections, layer));
            } else {
                lists[i] = ImmutableList.of();
            }
        }

        return lists[i];
    }

    public int getOutsideColor(EnumFacing facing, BlockRenderLayer layer) {
        return -1;
    }

    public int getInsideColor(EnumFacing facing, BlockRenderLayer layer) {
        return -1;
    }

    public boolean shouldRender(T target, BlockRenderLayer layer) {
        return true;
    }

    public abstract float getThickness(BlockRenderLayer layer);
    public abstract boolean isOpaque();
    public abstract TextureAtlasSprite getTexture(EnumFacing side, BlockRenderLayer layer, int connectionMatrix);

    protected List<BakedQuad> generateQuads(boolean[] connections, BlockRenderLayer layer) {
        List<BakedQuad> quads = new ArrayList<>();
        Vector3f from, to;

        for (EnumFacing facing : EnumFacing.VALUES) {
            EnumFacing[] neighbors = CONNECTION_DIRS[facing.ordinal()];
            int connectionMatrix = (connections[neighbors[0].ordinal()] ? 8 : 0) | (connections[neighbors[1].ordinal()] ? 4 : 0)
                    | (connections[neighbors[2].ordinal()] ? 2 : 0) | (connections[neighbors[3].ordinal()] ? 1 : 0);
            TextureAtlasSprite sprite = getTexture(facing, layer, connectionMatrix);
            float min = 8 - (getThickness(layer) / 2);
            float max = 8 + (getThickness(layer) / 2);
            int outsideColor = getOutsideColor(facing, layer);
            int insideColor = getInsideColor(facing, layer);
            if (!isOpaque() && connections[facing.ordinal()]) {
                // Connected; render up to four quads.
                if (connections[neighbors[2].ordinal()]) {
                    from = new Vector3f(0, min, min);
                    to = new Vector3f(min, min, max);
                    if (RENDER_OUTER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                    if (RENDER_INNER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }

                if (connections[neighbors[0].ordinal()]) {
                    from = new Vector3f(min, min, 0);
                    to = new Vector3f(max, min, min);
                    if (RENDER_OUTER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                    if (RENDER_INNER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }

                if (connections[neighbors[3].ordinal()]) {
                    from = new Vector3f(max, min, min);
                    to = new Vector3f(16, min, max);
                    if (RENDER_OUTER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                    if (RENDER_INNER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }

                if (connections[neighbors[1].ordinal()]) {
                    from = new Vector3f(min, min, max);
                    to = new Vector3f(max, min, 16);
                    if (RENDER_OUTER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                    if (RENDER_INNER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }
            } else {
                // Not connected; render one quad.
                from = new Vector3f(connections[neighbors[2].ordinal()] ? 0 : min, min, connections[neighbors[0].ordinal()] ? 0 : min);
                to = new Vector3f(connections[neighbors[3].ordinal()] ? 16 : max, min, connections[neighbors[1].ordinal()] ? 16 : max);
                if (RENDER_OUTER_FACES) quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, outsideColor, sprite, EnumFacing.DOWN, ROTATIONS[facing.ordinal()], true));
                if (RENDER_INNER_FACES && !isOpaque()) {
                    quads.add(CharsetFaceBakery.INSTANCE.makeBakedQuad(from, to, insideColor, sprite, EnumFacing.UP, ROTATIONS[facing.ordinal()], true));
                }
            }
        }
        return quads;
    }

    protected boolean connects(T target, BlockRenderLayer layer, EnumFacing facing) {
        return target.connects(facing);
    }

    public List<BakedQuad> getQuads(T target) {
        if (!shouldRender(target, MinecraftForgeClient.getRenderLayer())) {
            return Collections.emptyList();
        }

        if (target == null) {
            return getPipeQuads(0, MinecraftForgeClient.getRenderLayer());
        }

        int pointer = 0;
        for (EnumFacing f : EnumFacing.VALUES) {
            pointer = (pointer << 1) | (connects(target, MinecraftForgeClient.getRenderLayer(), f) ? 1 : 0);
        }

        return getPipeQuads(pointer, MinecraftForgeClient.getRenderLayer());
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }

        T target = null;
        if (state instanceof IExtendedBlockState) {
            target = ((IExtendedBlockState) state).getValue(property);
        }

        return getQuads(target);
    }
}