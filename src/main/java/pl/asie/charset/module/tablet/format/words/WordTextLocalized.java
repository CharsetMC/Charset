package pl.asie.charset.module.tablet.format.words;

import net.minecraft.util.text.translation.I18n;

public class WordTextLocalized extends WordText {
	public WordTextLocalized(String text) {
		super(I18n.translateToLocal(text));
	}
}
