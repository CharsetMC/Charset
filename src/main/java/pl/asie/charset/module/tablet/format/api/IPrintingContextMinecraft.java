package pl.asie.charset.module.tablet.format.api;

import net.minecraft.client.gui.FontRenderer;

import java.net.URI;

public interface IPrintingContextMinecraft extends IPrintingContext {
	FontRenderer getFontRenderer();
	boolean openURI(URI uri);
}
