package pl.asie.charset.module.tablet.format.parsers;

import pl.asie.charset.lib.utils.ThreeState;
import pl.asie.charset.module.tablet.TabletUtil;

import java.util.regex.Matcher;

public class GitHubMarkdownParser extends MarkdownParser {
	private final String prefix;
	private final String name;

	public GitHubMarkdownParser(String host, String name) {
		this.prefix = "https://github.com/" + host + "/wiki";
		this.name = name;
	}

	@Override
	public String parse(String text) {
		if (name.equals("Home")) {
			text = "[-> Sidebar](_Sidebar)\n\n" + text;
		} else if (name.equals("_Sidebar")) {
			text = "[<- Home](Home)\n\n" + text;
		}
		return super.parse(text);
	}

	@Override
	protected String urlHandler(Matcher m) {
		String url = m.group(2);
		if (url.startsWith(prefix)) {
			return "\\\\url{" + TabletUtil.encode(url.substring(prefix.length())) + "}{" + m.group(1) + "}";
		} else if (!url.contains("/")) {
			return "\\\\url{/" + TabletUtil.encode(url) + "}{" + m.group(1) + "}";
		} else {
			return "\\\\urlmissing{" + m.group(1) + "}";
		}
	}
}
