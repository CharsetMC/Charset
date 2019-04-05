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

import pl.asie.charset.module.tablet.format.api.IPrintingContextMinecraft;
import pl.asie.charset.module.tablet.format.api.IStyle;
import pl.asie.charset.module.tablet.format.api.WordPrinterMinecraft;
import pl.asie.charset.module.tablet.format.words.StyleColor;
import pl.asie.charset.module.tablet.format.words.StyleFormat;
import pl.asie.charset.module.tablet.format.words.WordBullet;

public class WordPrinterMCBullet extends WordPrinterMinecraft<WordBullet> {
	private static final String BULLET = " - ";

	@Override
	public int getWidth(IPrintingContextMinecraft context, WordBullet word) {
		return context.getFontRenderer().getCharWidth('-') * word.pad + context.getFontRenderer().getStringWidth(BULLET);
	}

	@Override
	public int getHeight(IPrintingContextMinecraft context, WordBullet word) {
		return context.getFontRenderer().FONT_HEIGHT;
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.BLOCK;
	}

	@Override
	public void draw(IPrintingContextMinecraft context, WordBullet word, int x, int y, boolean isHovering) {
		context.getFontRenderer().drawString(BULLET, context.getFontRenderer().getCharWidth('-') * word.pad + x, y, 0xFF000000);
	}
}
