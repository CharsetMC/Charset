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

package pl.asie.charset.module.crafting.compression;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.render.sprite.TextureAtlasSpriteCustom;
import pl.asie.charset.lib.utils.RenderUtils;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public final class CTMTextureFactory {
    protected static class SliceSprite extends TextureAtlasSpriteCustom {
        private final ResourceLocation location;
        private final boolean isLeft, isRight;

        protected SliceSprite(ResourceLocation loc, ResourceLocation location, int i) {
            super(loc.toString());
            this.location = location;
            this.isLeft = (i & 2) != 0;
            this.isRight = (i & 1) != 0;
        }

        @Override
        public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
            return true;
        }

        @Override
        public boolean load(IResourceManager manager, ResourceLocation loc, Function<ResourceLocation, TextureAtlasSprite> getter) {
            BufferedImage sheet = null;
            if (sheet == null) {
                sheet = RenderUtils.getTextureImage(location, null);
                if (sheet == null) {
                    ModCharset.logger.warn("Could not find texture sheet " + location + "!");
                    return false;
                }
            }

            int pieceWidth = sheet.getWidth() / 2;
            int pieceHeight = sheet.getHeight();
            int midX = (pieceWidth / 2);

            int[] pixels = new int[pieceWidth * pieceHeight];
            sheet.getRGB(pieceWidth * (isLeft ? 1 : 0), 0,  midX, pieceHeight, pixels, 0, pieceWidth);
            sheet.getRGB(pieceWidth * (isRight ? 1 : 0) + midX, 0, midX, pieceHeight, pixels, midX, pieceWidth);

            this.addFrameTextureData(pieceWidth, pieceHeight, pixels);
            return false;
        }
    }

    private CTMTextureFactory() {

    }

    // 0 - NN, 1 - NY, 2 - YN, 3 - YY
    public static TextureAtlasSprite[] register(TextureMap map, ResourceLocation location) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[4];
        for (int i = 0; i < 4; i++) {
            String s = String.format(location.toString() + "#%d", i);
            sprites[i] = map.getTextureExtry(s);
            if (sprites[i] == null) {
                sprites[i] = new SliceSprite(new ResourceLocation(s), location, i);
                map.setTextureEntry(sprites[i]);
            }
        }
        return sprites;
    }
}
