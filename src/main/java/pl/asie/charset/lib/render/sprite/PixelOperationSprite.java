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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.utils.RenderUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public class PixelOperationSprite extends TextureAtlasSpriteCustom {
    @FunctionalInterface
    public interface Operator {
        void apply(int[] pixels, int width, Function<ResourceLocation, TextureAtlasSprite> getter);
    }

    @FunctionalInterface
    public interface OperatorForEach {
        int apply(int x, int y, int value);
    }

    public static Operator combine(Operator... ops) {
        return ((pixels, width, getter) -> {
            for (Operator o : ops) {
                o.apply(pixels, width, getter);
            }
        });
    }

    public static Operator forEach(OperatorForEach op) {
        return ((pixels, width, getter) -> {
            int i = 0;
            for (int y = 0; y < pixels.length/width; y++) {
                for (int x = 0; x < width; x++, i++) {
                    pixels[i] = op.apply(x, y, pixels[i]);
                }
            }
        });
    }

    public static OperatorForEach multiply(int color) {
        return (x, y, value) -> RenderUtils.multiplyColor(value, color);
    }

    private final ResourceLocation location;
    private final Operator operator;
    private final Collection<ResourceLocation> deps;

    public PixelOperationSprite(String entry, ResourceLocation location, Operator operator, ResourceLocation... deps) {
        super(entry);
        this.location = location;
        this.operator = operator;
        ImmutableSet.Builder<ResourceLocation> depBuilder = new ImmutableSet.Builder<>();
        if (!entry.equals(location.toString())) {
            depBuilder.add(location);
        }
        for (ResourceLocation dep : deps) {
            depBuilder.add(dep);
        }
        this.deps = depBuilder.build();
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation loc, Function<ResourceLocation, TextureAtlasSprite> getter) {
        int[] pixels = null;
        int width = 0, height = 0;

        if (deps.contains(location)) {
            TextureAtlasSprite sprite = getter.apply(location);
            try {
                if (sprite != null) {
                    int frameSize = sprite.getIconWidth() * sprite.getIconHeight();
                    pixels = new int[frameSize * sprite.getFrameCount()];
                    for (int i = 0; i < sprite.getFrameCount(); i++) {
                        System.arraycopy(sprite.getFrameTextureData(i)[0], 0, pixels, i * frameSize, frameSize);
                    }
                    width = sprite.getIconWidth();
                    height = sprite.getIconHeight() * sprite.getFrameCount();
                }
            } catch (Exception e) {
                pixels = null;
            }
        }

        if (pixels == null) {
            BufferedImage image = RenderUtils.getTextureImage(location, getter);
            if (image == null) {
                return false;
            }

            pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
            width = image.getWidth();
            height = image.getHeight();
        }

        operator.apply(pixels, width, getter);

        try {
            this.addFrameTextureData(width, height, pixels, manager.getResource(RenderUtils.toTextureFilePath(location)));
        } catch (IOException e) {
            e.printStackTrace();
            this.addFrameTextureData(width, height, pixels);
        }

        return false;
    }

    @Override
    public java.util.Collection<ResourceLocation> getDependencies() {
        return deps;
    }
}
