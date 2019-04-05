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

package pl.asie.charset.lib.render;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.ITransformation;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.utils.RenderUtils;

public class CharsetFaceBakery extends FaceBakery {
    public static final CharsetFaceBakery INSTANCE = new CharsetFaceBakery();

    private CharsetFaceBakery() {

    }

    private static final float[] faceBrightness = new float[] {
            0.5f, 1.0f, 0.8f, 0.8f, 0.6f, 0.6f
    };

    public static float getFaceBrightness(EnumFacing facing) {
        return facing == null ? 1.0f : faceBrightness[facing.ordinal()];
    }

    public BakedQuad makeBakedQuad(Vector3f min, Vector3f max, int tintIndex,
                                   TextureAtlasSprite icon, EnumFacing facing, ITransformation rot, boolean uvLocked) {
        return makeBakedQuad(min, max, tintIndex, RenderUtils.calculateUV(min, max, facing), icon, facing, rot, uvLocked);
    }

    public BakedQuad makeBakedQuad(Vector3f min, Vector3f max, int tintIndex, float[] uv,
                                   TextureAtlasSprite icon, EnumFacing facing, ITransformation rot, boolean uvLocked) {
        if (icon == null) {
            return null;
        }

        boolean hasColorIndex = tintIndex != -1 && ((tintIndex & 0xFF000000) == 0);
        boolean hasColor = tintIndex != -1 && ((tintIndex & 0xFF000000) != 0);

        BakedQuad quad = makeBakedQuad(
                min, max,
                new BlockPartFace(null, hasColorIndex ? tintIndex : -1, "", new BlockFaceUV(uv, 0)),
                icon, facing, rot, null, uvLocked, true
        );

        if (hasColor) {
           recolorQuad(quad, tintIndex);
        }

        return quad;
    }

    private BakedQuad recolorQuad(BakedQuad quad, int color) {
        int c = quad.getFormat().getColorOffset() / 4;
        int v = quad.getFormat().getIntegerSize();
        int[] vertexData = quad.getVertexData();
        for (int i = 0; i < 4; i++) {
            vertexData[v * i + c] = RenderUtils.multiplyColor(vertexData[v * i + c], color);
        }
        return quad;
    }
}
