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
		return 0;
	}

	public abstract void draw(IPrintingContextMinecraft context, T word, int x, int y, boolean isHovering);
	public void drawTooltip(IPrintingContextMinecraft context, T word, int mouseX, int mouseY) {

	}

	public boolean onClick(IPrintingContextMinecraft context, T word) {
		return false;
	}
}
