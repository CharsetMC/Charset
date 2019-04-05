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

package pl.asie.charset.module.tablet.format.words.minecraft;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import pl.asie.charset.ModCharset;
import pl.asie.charset.module.tablet.format.api.IPrintingContextMinecraft;
import pl.asie.charset.module.tablet.format.api.IStyle;
import pl.asie.charset.module.tablet.format.api.WordPrinterMinecraft;
import pl.asie.charset.module.tablet.format.words.StyleColor;
import pl.asie.charset.module.tablet.format.words.StyleFormat;
import pl.asie.charset.module.tablet.format.words.StyleScale;
import pl.asie.charset.module.tablet.format.words.WordText;

public class WordPrinterMCText extends WordPrinterMinecraft<WordText> {
	@Override
	public int getWidth(IPrintingContextMinecraft context, WordText word) {
		StringBuilder s = new StringBuilder(word.getText());
		float scale = 1.0f;
		for (IStyle style : context.getStyleList()) {
			if (style instanceof StyleFormat) {
				s.insert(0, ((StyleFormat) style).getMcPrefix());
			} else if (style instanceof StyleScale) {
				scale *= ((StyleScale) style).scale;
			}
		}
		return (int) Math.ceil(context.getFontRenderer().getStringWidth(s.toString()) * scale);
	}

	@Override
	public int getHeight(IPrintingContextMinecraft context, WordText word) {
		float scale = 1.0f;
		for (IStyle style : context.getStyleList()) {
			if (style instanceof StyleScale) {
				scale *= ((StyleScale) style).scale;
			}
		}
		return (int) Math.ceil(context.getFontRenderer().FONT_HEIGHT * scale);
	}

	@Override
	public void draw(IPrintingContextMinecraft context, WordText word, int x, int y, boolean isHovering) {
		StringBuilder s = new StringBuilder(word.getText());
		float scale = 1.0f;
		int color = 0xFF000000;
		for (IStyle style : context.getStyleList()) {
			if (style instanceof StyleFormat) {
				s.insert(0, ((StyleFormat) style).getMcPrefix());
			} else if (style instanceof StyleColor) {
				color = 0xFF000000 | ((StyleColor) style).color;
			} else if (style instanceof StyleScale) {
				scale *= ((StyleScale) style).scale;
			}
		}
		if (scale == 1.0f) {
			context.getFontRenderer().drawString(s.toString(), x, y, color);
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);
			GlStateManager.scale(scale, scale, scale);
			context.getFontRenderer().drawString(s.toString(), 0, 0, color);
			GlStateManager.popMatrix();
		}
	}
}
