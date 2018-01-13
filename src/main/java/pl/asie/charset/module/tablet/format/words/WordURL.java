package pl.asie.charset.module.tablet.format.words;

import java.net.URI;

public class WordURL extends WordText {
	private final URI uri;

	public WordURL(String text, URI uri) {
		super(text);
		this.uri = uri;
	}

	public WordURL(String text, URI uri, float scale) {
		super(text, scale);
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}
}
