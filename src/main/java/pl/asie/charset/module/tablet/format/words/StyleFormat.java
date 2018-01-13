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
