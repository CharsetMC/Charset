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

package pl.asie.charset.lib.render;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.lwjgl.util.vector.Vector3f;
import pl.asie.charset.lib.utils.RenderUtils;

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
