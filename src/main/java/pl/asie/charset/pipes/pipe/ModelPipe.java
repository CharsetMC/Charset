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

package pl.asie.charset.pipes.pipe;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.render.model.ModelPipeShaped;

public class ModelPipe extends ModelPipeShaped<TilePipe> {
    public static final ResourceLocation PIPE_TEXTURE_LOC = new ResourceLocation("charset", "blocks/pipe");
    public static TextureAtlasSprite[] sprites;

    public ModelPipe() {
        super(TilePipe.PROPERTY);
    }

    @Override
    public float getThickness() {
        return 7.995f;
    }

    @Override
    public int getInsideColor(EnumFacing facing) {
        return facing.getAxis() == EnumFacing.Axis.Y ? 0xFFA0A0A0 : 0xFFE0E0E0;
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
