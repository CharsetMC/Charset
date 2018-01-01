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

package pl.asie.charset.module.tweak;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.render.sprite.PixelOperationSprite;
import pl.asie.charset.lib.resources.ColorPaletteParser;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.RenderUtils;

import java.awt.image.BufferedImage;
import java.util.Arrays;

@CharsetModule(
		name = "tweak.unifyColors",
		profile = ModuleProfile.EXPERIMENTAL,
		isDefault = false,
		description = "Unifies various colored block and item colors. Works with resource packs!",
		isClientOnly = true
)
public class CharsetTweakUnifyColors {
	private int colorMultiplier(String prefix, EnumDyeColor color) {
		float[] d = ColorUtils.getDyeRgb(color);

		if (prefix.contains("hardened_clay")) {
			float[] dOrig = d;
			d = Arrays.copyOf(dOrig, 3);

			float lum = d[0] * 0.3F + d[1] * 0.59F + d[2] * 0.11F;
			float mul = (color == EnumDyeColor.YELLOW || color == EnumDyeColor.ORANGE || color == EnumDyeColor.RED) ? 0.6f : 0.7f;
			d[0] *= 0.9F;
			d[1] *= 0.9F;
			d[2] *= 0.9F;
			d[0] += (lum - d[0]) * mul;
			d[1] += (lum - d[1]) * mul;
			d[2] += (lum - d[2]) * mul;
		}

		return RenderUtils.asMcIntColor(d);
	}

	private BufferedImage toGrayscale(BufferedImage image) {
		BufferedImage imageGrayscale = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		for (int iy = 0; iy < image.getHeight(); iy++) {
			for (int ix = 0; ix < image.getWidth(); ix++) {
				int c = image.getRGB(ix, iy);
				int g = Math.round((c & 0xFF) * 0.11F + ((c >> 8) & 0xFF) * 0.59F + ((c >> 16) & 0xFF) * 0.3F);
				int o = (c & 0xFF000000) | (g * 0x10101);
				imageGrayscale.setRGB(ix, iy, o);
			}
		}
		return imageGrayscale;
	}

	private int[] computeMinMaxData(BufferedImage image) {
		int[] out = new int[] {1,1,1,0,255,255,255,0,0,0,0,0};
		for (int iy = 0; iy < image.getHeight(); iy++) {
			for (int ix = 0; ix < image.getWidth(); ix++) {
				int c = image.getRGB(ix, iy);
				int g = Math.round((c & 0xFF) * 0.11F + ((c >> 8) & 0xFF) * 0.59F + ((c >> 16) & 0xFF) * 0.3F);
				if ((c & 0xFF) > out[0]) out[0] = (c & 0xFF);
				if (((c>>8) & 0xFF) > out[1]) out[1] = ((c >> 8) & 0xFF);
				if (((c>>16) & 0xFF) > out[2]) out[2] = ((c >> 16) & 0xFF);
				if ((c & 0xFF) < out[4]) out[4] = (c & 0xFF);
				if (((c>>8) & 0xFF) < out[5]) out[5] = ((c >> 8) & 0xFF);
				if (((c>>16) & 0xFF) < out[6]) out[6] = ((c >> 16) & 0xFF);
				out[8] += (c & 0xFF);
				out[9] += ((c >> 8) & 0xFF);
				out[10] += ((c >> 16) & 0xFF);
				out[11] += g;
			}
		}
		out[3] = Math.max(out[0], Math.max(out[1], out[2]));
		out[7] = Math.min(out[4], Math.min(out[5], out[6]));
		for (int i = 8; i < 12; i++)
			out[i] /= image.getWidth()*image.getHeight();
		return out;
	}

	private void recolorTextures(TextureMap map, String prefix) {
		ResourceLocation source = new ResourceLocation(prefix + "white");
		for (int i = 0; i < 16; i++) {
			String s = ColorUtils.getUnderscoredSuffix(EnumDyeColor.byMetadata(i));
			ResourceLocation target = new ResourceLocation(prefix + s);
			if (prefix.contains("hardened_clay")) {
				BufferedImage image = RenderUtils.getTextureImage(new ResourceLocation("minecraft:blocks/hardened_clay"));
				BufferedImage imageGrayscale = toGrayscale(image);
				int[] imageData = computeMinMaxData(image);
				int[] imageGrayData = computeMinMaxData(imageGrayscale);
				int delta = imageGrayData[3] - imageGrayData[7];
				final float divisor = delta > 5 ? (float) delta / 5.0f : 1.0f;
				final int value2 = colorMultiplier(prefix, EnumDyeColor.byMetadata(i));

				map.setTextureEntry(new PixelOperationSprite(target.toString(), source) {
					@Override
					public int apply(int x, int y, int value) {
						int out = 0xFF000000;
						for (int i = 0; i < 24; i += 8) {
							int v1 = (((imageGrayscale.getRGB(x, y) >> i) & 0xFF) * 255 / imageGrayData[i >> 3]) - 0xFF;
							v1 /= divisor;
							int v2 = ((value2 >> i) & 0xFF) + v1;
							if (v2 < 0) v2 = 0;
							if (v2 > 255) v2 = 255;
							int nonTintedOut = (v2 & 0xFF);
							int tintedOut = nonTintedOut * imageData[8 + (i >> 3)] / imageData[0 + 3];
							out |= Math.round((nonTintedOut + tintedOut + (tintedOut / 2)) / 2.5f) << i;
						}
						return out;
					}
				});
			} else if (i > 0) { // skip white for non-clay
				map.setTextureEntry(new PixelOperationSprite.Multiply(target.toString(), source, colorMultiplier(prefix, EnumDyeColor.byMetadata(i))));
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		try {
			for (int i = 0; i < 16; i++) {
				EnumDyeColor color = EnumDyeColor.byMetadata(i);
				String key = ColorUtils.getUnderscoredSuffix(color);
				double[] data = ColorPaletteParser.INSTANCE.getColor("minecraft:dye", key);
				if (data != null && (data.length == 3 || (data.length > 3 && data[3] > 0.0))) {
					float[] dst = ColorUtils.getDyeRgb(color);
					dst[0] = (float) data[0];
					dst[1] = (float) data[1];
					dst[2] = (float) data[2];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		recolorTextures(event.getMap(), "minecraft:blocks/wool_colored_");
		recolorTextures(event.getMap(), "minecraft:blocks/glass_");
		recolorTextures(event.getMap(), "minecraft:blocks/glass_pane_top_");
		recolorTextures(event.getMap(), "minecraft:blocks/hardened_clay_stained_");
		recolorTextures(event.getMap(), "minecraft:blocks/concrete_");
		recolorTextures(event.getMap(), "minecraft:blocks/concrete_powder_");
		recolorTextures(event.getMap(), "minecraft:blocks/shulker_top_");
	}
}
