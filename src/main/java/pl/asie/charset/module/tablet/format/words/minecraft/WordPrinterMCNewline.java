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
		return context.getFontRenderer().FONT_HEIGHT;
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
