/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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
import pl.asie.charset.module.tablet.format.api.WordPrinterMinecraft;
import pl.asie.charset.module.tablet.format.words.WordNewline;

public class WordPrinterMCNewline extends WordPrinterMinecraft<WordNewline> {
	@Override
	public int getWidth(IPrintingContextMinecraft context, WordNewline word) {
		return 0;
	}

	@Override
	public int getPaddingAbove(IPrintingContextMinecraft context, WordNewline word) {
		return (word.getLines() - 1) * context.getFontRenderer().FONT_HEIGHT;
	}

	@Override
	public int getHeight(IPrintingContextMinecraft context, WordNewline word) {
		return context.getFontRenderer().FONT_HEIGHT;
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.BLOCK;
	}

	@Override
	public void draw(IPrintingContextMinecraft context, WordNewline word, int x, int y, boolean isHovering) {
	}
}
