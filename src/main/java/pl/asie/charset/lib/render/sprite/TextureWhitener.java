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

package pl.asie.charset.lib.render.sprite;

import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.lib.utils.colorspace.Colorspace;
import pl.asie.charset.lib.utils.colorspace.Colorspaces;

import java.awt.image.BufferedImage;
import java.util.function.BiPredicate;
import java.util.function.Function;

// Remember to brush your teeth!
public class TextureWhitener {
	public static final TextureWhitener INSTANCE = new TextureWhitener();
	private final TObjectFloatMap<ResourceLocation> lumaMap = new TObjectFloatHashMap<>();
	private final TObjectIntMap<ResourceLocation> alphaMap = new TObjectIntHashMap<>();

	private void findMaxLumaAlpha(ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> getter, BiPredicate<Float, Float> usePixel) {
		BufferedImage image = RenderUtils.getTextureImage(location, getter);
		float luma = 0.0f;
		int alpha = 0;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (usePixel != null && !usePixel.test(x / (float) image.getWidth(), y / (float) image.getHeight()))
					continue;

				int a = (image.getRGB(x, y) >> 24) & 0xFF;
				if (a > 0) {
					if (alpha < a) {
						alpha = a;
					}

					float[] vals = Colorspaces.convertFromRGB(image.getRGB(x, y), Colorspace.YUV);
					if (luma < vals[0]) {
						luma = vals[0];
					}
				}
			}
		}

		lumaMap.put(location, luma);
		alphaMap.put(location, alpha);
	}

	public void clear() {
		lumaMap.clear();
		alphaMap.clear();
	}

	public TextureAtlasSprite remap(ResourceLocation sourceTexture, ResourceLocation destTexture, ResourceLocation colorTexture) {
		return remap(sourceTexture, destTexture, colorTexture, -1);
	}

	public TextureAtlasSprite remap(ResourceLocation sourceTexture, ResourceLocation destTexture, ResourceLocation colorTexture, int newAlpha) {
		return remap(sourceTexture, destTexture, colorTexture, newAlpha, null);
	}

	public void remap(int[] pixels, int width, Function<ResourceLocation, TextureAtlasSprite> getter, ResourceLocation colorTexture, int newAlpha, BiPredicate<Float, Float> usePixel, float lumaMultiplier, boolean preserveColor) {
		if (!lumaMap.containsKey(colorTexture)) {
			findMaxLumaAlpha(colorTexture, getter, usePixel);
		}

		float luma = lumaMap.get(colorTexture);
		int a = alphaMap.get(colorTexture);
		int height = pixels.length / width;

		PixelOperationSprite.forEach((x, y, value) -> {
			if (usePixel != null && !usePixel.test(x / (float) width, y / (float) height))
				return value;

			float[] vals = Colorspaces.convertFromRGB(value, Colorspace.YUV);
			vals[0] *= (1.0f / luma) * lumaMultiplier;
			if (!preserveColor) {
				vals[1] = 0.0f;
				vals[2] = 0.0f;
			}
			int alpha = ((value >> 24) & 0xFF);
			if (newAlpha > 0 && alpha > 0) {
				alpha = newAlpha;
//			} else {
//				alpha = (int) ((float) alpha * 255.0f / (float) a);
			}
			return (alpha << 24) | (Colorspaces.convertToRGB(vals, Colorspace.YUV) & 0xFFFFFF);
		}).apply(pixels, width, getter);
	}

	public TextureAtlasSprite remap(ResourceLocation sourceTexture, ResourceLocation destTexture, ResourceLocation colorTexture, int newAlpha, BiPredicate<Float, Float> usePixel) {
		if (!lumaMap.isEmpty()) {
			lumaMap.clear();
			alphaMap.clear();
		}

		return new PixelOperationSprite(destTexture.toString(), sourceTexture,
				((pixels, width, getter) -> remap(pixels,width, getter, colorTexture, newAlpha, usePixel, 1.0f, false)),
				colorTexture);
	}
}
