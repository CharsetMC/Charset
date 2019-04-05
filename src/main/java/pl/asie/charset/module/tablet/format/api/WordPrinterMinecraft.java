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

package pl.asie.charset.module.tablet.format.api;

import net.minecraft.client.gui.FontRenderer;

public abstract class WordPrinterMinecraft<T extends Word> {
	public enum DisplayType {
		INLINE, BLOCK
	};

	public DisplayType getDisplayType() {
		return DisplayType.INLINE;
	}

	public abstract int getWidth(IPrintingContextMinecraft context, T word);
	public abstract int getHeight(IPrintingContextMinecraft context, T word);
	public int getPaddingAbove(IPrintingContextMinecraft context, T word) {
		return 1;
	}

	public abstract void draw(IPrintingContextMinecraft context, T word, int x, int y, boolean isHovering);
	public void drawTooltip(IPrintingContextMinecraft context, T word, int mouseX, int mouseY) {

	}

	public boolean onClick(IPrintingContextMinecraft context, T word) {
		return false;
	}
}
