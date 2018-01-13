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
		return context.getFontRenderer().getStringWidth(BULLET);
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
		context.getFontRenderer().drawString(BULLET, x, y, 0xFF000000);
	}
}
