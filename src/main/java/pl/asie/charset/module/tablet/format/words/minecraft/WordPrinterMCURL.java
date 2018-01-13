package pl.asie.charset.module.tablet.format.words.minecraft;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import pl.asie.charset.module.tablet.format.api.IPrintingContextMinecraft;
import pl.asie.charset.module.tablet.format.api.IStyle;
import pl.asie.charset.module.tablet.format.api.WordPrinterMinecraft;
import pl.asie.charset.module.tablet.format.words.StyleColor;
import pl.asie.charset.module.tablet.format.words.StyleFormat;
import pl.asie.charset.module.tablet.format.words.WordURL;

public class WordPrinterMCURL extends WordPrinterMinecraft<WordURL> {
	@Override
	public int getWidth(IPrintingContextMinecraft context, WordURL word) {
		StringBuilder s = new StringBuilder(word.getText());
		for (IStyle style : context.getStyleList()) {
			if (style instanceof StyleFormat) {
				s.insert(0, ((StyleFormat) style).getMcPrefix());
			}
		}
		s.insert(0, TextFormatting.UNDERLINE);
		return context.getFontRenderer().getStringWidth(s.toString());
	}

	@Override
	public boolean onClick(IPrintingContextMinecraft context, WordURL word) {
		return context.openURI(word.getUri());
	}

	@Override
	public int getHeight(IPrintingContextMinecraft context, WordURL word) {
		return context.getFontRenderer().FONT_HEIGHT;
	}

	@Override
	public void draw(IPrintingContextMinecraft context, WordURL word, int x, int y, boolean isHovering) {
		StringBuilder s = new StringBuilder(word.getText());
		int color = 0xFF3333CC;
		for (IStyle style : context.getStyleList()) {
			if (style instanceof StyleFormat) {
				s.insert(0, ((StyleFormat) style).getMcPrefix());
			} else if (style instanceof StyleColor) {
				color = 0xFF000000 | ((StyleColor) style).color;
			}
		}
		s.insert(0, TextFormatting.UNDERLINE);
		if (word.getScale() == 1.0f) {
			context.getFontRenderer().drawString(s.toString(), x, y, color);
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);
			GlStateManager.scale(word.getScale(), word.getScale(), word.getScale());
			context.getFontRenderer().drawString(s.toString(), 0, 0, color);
			GlStateManager.popMatrix();
		}
	}
}
