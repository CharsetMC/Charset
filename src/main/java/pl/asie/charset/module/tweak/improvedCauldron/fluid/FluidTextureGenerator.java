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

package pl.asie.charset.module.tweak.improvedCauldron.fluid;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.render.sprite.PixelOperationSprite;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.lib.utils.colorspace.Colorspace;
import pl.asie.charset.lib.utils.colorspace.Colorspaces;
import pl.asie.charset.module.tweak.improvedCauldron.CharsetTweakImprovedCauldron;

import java.awt.image.BufferedImage;

public class FluidTextureGenerator {
	private static final ResourceLocation WATER_STILL = new ResourceLocation("minecraft:blocks/water_still");
	private static final ResourceLocation WATER_FLOWING = new ResourceLocation("minecraft:blocks/water_flow");

	private float calcLuma(ResourceLocation oldLoc) {
		BufferedImage image = RenderUtils.getTextureImage(oldLoc);
		float luma = 0.0f;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				float[] vals = Colorspaces.convertFromRGB(image.getRGB(x, y), Colorspace.LAB);
				if (luma < vals[0]) {
					luma = vals[0];
				}
			}
		}

		return luma;
	}

	private int calcAlpha(ResourceLocation oldLoc) {
		BufferedImage image = RenderUtils.getTextureImage(oldLoc);
		int alpha = 0;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int a = (image.getRGB(x, y) >> 24) & 0xFF;
				if (alpha < a) {
					alpha = a;
				}
			}
		}

		return alpha;
	}

	private void remap(TextureMap map, ResourceLocation oldLoc, ResourceLocation newLoc, float luma, int a) {
		map.setTextureEntry(new PixelOperationSprite(newLoc.toString(), oldLoc, (x, y, value) -> {
			float[] vals = Colorspaces.convertFromRGB(value, Colorspace.LAB);
			vals[0] *= 100.0f / luma;
			vals[1] = 0.0f;
			vals[2] = 0.0f;
			int alpha = ((value >> 24) & 0xFF);
			alpha = (int) ((float) alpha * 255.0f / (float) a);
			return (alpha << 24) | (Colorspaces.convertToRGB(vals, Colorspace.LAB) & 0xFFFFFF);
		}));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		float luma = calcLuma(WATER_STILL);
		int alpha = calcAlpha(WATER_STILL);
		CharsetTweakImprovedCauldron.waterAlpha = alpha;

		remap(event.getMap(), WATER_STILL, FluidDyedWater.TEXTURE_STILL, luma, alpha);
		remap(event.getMap(), WATER_FLOWING, FluidDyedWater.TEXTURE_FLOW, luma, alpha);
	}
}
