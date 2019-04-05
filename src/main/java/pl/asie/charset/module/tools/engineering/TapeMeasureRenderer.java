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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TapeMeasureRenderer implements IItemColor {
	public static final TapeMeasureRenderer INSTANCE = new TapeMeasureRenderer();
	private TextureAtlasSprite tape;

	private TapeMeasureRenderer() {

	}

	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		tape = event.getMap().registerSprite(new ResourceLocation("charset:misc/tape_measure_tape"));
	}

	@SubscribeEvent
	public void onItemColor(ColorHandlerEvent.Item event) {
//		event.getItemColors().registerItemColorHandler(this, CharsetToolsEngineering.tapeMeasure);
	}

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		if (tintIndex != 1) {
			return -1;
		}

		// TODO
		return 0xFFF1EC5D;
	}
}
