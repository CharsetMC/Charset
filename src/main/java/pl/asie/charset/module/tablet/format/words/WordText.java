package pl.asie.charset.module.tablet.format.words;

import pl.asie.charset.module.tablet.format.api.Word;

import java.util.Arrays;
import java.util.EnumSet;

public class WordText extends Word {
	private final String text;
	private final float scale;

	public WordText(String text) {
		this(text, 1.0f);
	}

	public WordText(String text, float scale) {
		this.text = text;
		this.scale = scale;
	}

	public String getText() {
		return text;
	}

	public float getScale() {
		return scale;
	}

	@Override
	public String toString() {
		return "TextWord{" + text + "}";
	}
}
