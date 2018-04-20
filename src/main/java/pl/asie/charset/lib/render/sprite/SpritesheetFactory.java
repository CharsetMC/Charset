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

package pl.asie.charset.lib.render.sprite;

import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.RenderUtils;

import java.awt.image.BufferedImage;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SpritesheetFactory {
    protected static class SliceSprite extends TextureAtlasSpriteCustom {
        private final ResourceLocation location;
        private final int x, y, width, height;

        protected SliceSprite(ResourceLocation loc, ResourceLocation location, int i, int width, int height) {
            super(loc.toString());
            this.location = location;
            this.width = width;
            this.height = height;
            this.x = i % width;
            this.y = i / width;
        }

        @Override
        public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
            return true;
        }

        @Override
        public boolean load(IResourceManager manager, ResourceLocation loc, Function<ResourceLocation, TextureAtlasSprite> getter) {
            BufferedImage sheet = RenderUtils.getTextureImage(location, getter);
            if (sheet == null) {
                ModCharset.logger.warn("Could not find texture sheet " + location + "!");
                return false;
            }

            int pieceWidth = sheet.getWidth() / width;
            int pieceHeight = sheet.getHeight() / height;

            int[] pixels = new int[pieceWidth * pieceHeight];
            sheet.getRGB(pieceWidth * x, pieceHeight * y, pieceWidth, pieceHeight, pixels, 0, pieceWidth);

            this.addFrameTextureData(pieceWidth, pieceHeight, pixels);
            return false;
        }
    }

    private SpritesheetFactory() {

    }

    public static TextureAtlasSprite[] register(TextureMap map, ResourceLocation location, int width, int height) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[width * height];
        for (int i = 0; i < sprites.length; i++) {
            String s = String.format(location.toString() + "#%d", i);
            sprites[i] = map.getTextureExtry(s);
            if (sprites[i] == null) {
                sprites[i] = new SliceSprite(new ResourceLocation(s), location, i, width, height);
                map.setTextureEntry(sprites[i]);
            }
        }
        return sprites;
    }
}
