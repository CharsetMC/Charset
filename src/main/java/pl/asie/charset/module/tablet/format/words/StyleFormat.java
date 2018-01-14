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

package pl.asie.charset.module.tablet.format.words;

import net.minecraft.util.text.TextFormatting;
import pl.asie.charset.module.tablet.format.api.IStyle;
import pl.asie.charset.module.tablet.format.api.TextPrinterFormat;

public enum StyleFormat implements IStyle {
	BOLD("b", "**", TextFormatting.BOLD),
	ITALIC("i", "*", TextFormatting.ITALIC),
	UNDERLINE("u", "_", TextFormatting.UNDERLINE),
	STRIKETHROUGH("del", "~", TextFormatting.STRIKETHROUGH);

	private final String htmlTag;
	private final String mdWrap;
	private final TextFormatting mcPrefix;

	StyleFormat(String htmlTag, String mdWrap, TextFormatting mcPrefix) {
		this.htmlTag = htmlTag;
		this.mdWrap = mdWrap;
		this.mcPrefix = mcPrefix;
	}

	public TextFormatting getMcPrefix() {
		return mcPrefix;
	}

	public String getStart(TextPrinterFormat format) {
		switch (format) {
			case HTML:
				return "<" + htmlTag + ">";
			case MARKDOWN:
				return mdWrap;
			default:
				return "?" + htmlTag + "?";
		}
	}

	public String getEnd(TextPrinterFormat format) {
		switch (format) {
			case HTML:
				return "</" + htmlTag + ">";
			case MARKDOWN:
				return mdWrap;
			default:
				return "?" + htmlTag + "?";
		}
	}
}
