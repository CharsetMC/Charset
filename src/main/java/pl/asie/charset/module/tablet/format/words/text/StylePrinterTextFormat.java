package pl.asie.charset.module.tablet.format.words.text;

import pl.asie.charset.module.tablet.format.api.IPrintingContext;
import pl.asie.charset.module.tablet.format.api.StylePrinterText;
import pl.asie.charset.module.tablet.format.api.TextPrinterFormat;
import pl.asie.charset.module.tablet.format.words.StyleFormat;

public class StylePrinterTextFormat implements StylePrinterText<StyleFormat> {
	private final TextPrinterFormat format;

	public StylePrinterTextFormat(TextPrinterFormat format) {
		this.format = format;
	}

	@Override
	public String push(IPrintingContext context, StyleFormat style) {
		return style.getStart(format);
	}

	@Override
	public String pop(IPrintingContext context, StyleFormat style) {
		return style.getEnd(format);
	}
}
