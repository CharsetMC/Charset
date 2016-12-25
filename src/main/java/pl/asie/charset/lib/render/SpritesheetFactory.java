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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.utils.RenderUtils;

import java.awt.image.BufferedImage;

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
        public boolean load(IResourceManager manager, ResourceLocation loc) {
            if (sheet == null) {
                sheet = RenderUtils.getTextureImage(location);
                if (sheet == null) {
                    ModCharsetLib.logger.warn("Could not find texture sheet " + location + "!");
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
