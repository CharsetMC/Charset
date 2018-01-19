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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.utils.RenderUtils;

import java.awt.image.BufferedImage;
import java.util.function.Function;

public class PixelOperationSprite extends TextureAtlasSprite {
    @FunctionalInterface
    public interface Operator {
        int apply(int x, int y, int value);
    }

    public static Operator multiply(int color) {
        return (x, y, value) -> RenderUtils.multiplyColor(value, color);
    }

    private final ResourceLocation location;
    private final Operator operator;

    public PixelOperationSprite(String entry, ResourceLocation location, Operator operator) {
        super(entry);
        this.location = location;
        this.operator = operator;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation loc, Function<ResourceLocation, TextureAtlasSprite> getter) {
        BufferedImage image = RenderUtils.getTextureImage(location);
        if (image == null) {
            return false;
        }

        setIconWidth(image.getWidth());
        setIconHeight(image.getHeight());

        int[][] pixels = new int[Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels() + 1][];
        pixels[0] = new int[image.getWidth() * image.getHeight()];

        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels[0], 0, image.getWidth());
        for (int i = 0; i < pixels[0].length; i++) {
            pixels[0][i] = operator.apply(i % image.getWidth(), i / image.getWidth(), pixels[0][i]);
        }

        this.clearFramesTextureData();
        this.framesTextureData.add(pixels);

        return false;
    }
}
