/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.utils.RenderUtils;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class SpritesheetFactory {
    private final ResourceLocation location;
    private final int width, height;

    protected static class SliceSprite extends TextureAtlasSprite {
        private final ResourceLocation location;
        private final int x, y, width, height;
        private BufferedImage sheet;

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
            if (sheet == null) {
                sheet = RenderUtils.getTextureImage(location);
                if (sheet == null) {
                    ModCharset.logger.warn("Could not find texture sheet " + location + "!");
                    return false;
                }
            }

            int pieceWidth = sheet.getWidth() / width;
            int pieceHeight = sheet.getHeight() / height;

            setIconWidth(pieceWidth);
            setIconHeight(pieceHeight);

            int[][] pixels = new int[Minecraft.getMinecraft().gameSettings.mipmapLevels + 1][];
            pixels[0] = new int[pieceWidth * pieceHeight];

            sheet.getRGB(pieceWidth * x, pieceHeight * y, pieceWidth, pieceHeight, pixels[0], 0, pieceWidth);

            this.clearFramesTextureData();
            this.framesTextureData.add(pixels);

            return false;
        }
    }

    private SpritesheetFactory(ResourceLocation location, int width, int height) {
        this.location = location;
        this.width = width;
        this.height = height;
    }

    public static TextureAtlasSprite[] register(TextureMap map, ResourceLocation location, int width, int height) {
        SpritesheetFactory factory = new SpritesheetFactory(location, width, height);
        return factory.register(map);
    }

    public TextureAtlasSprite[] register(TextureMap map) {
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
