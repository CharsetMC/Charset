package pl.asie.charset.module.tablet.format.parsers;

import java.util.regex.Pattern;

public class MarkdownParser {
	public MarkdownParser() {

	}

	public String parse(String text) {
		// TODO!
		String[] textLines = text.split("\n");
		StringBuilder builder = new StringBuilder();
		int headerCount = 0;
		for (int i = 0; i < textLines.length; i++) {
			String line = textLines[i];

			if ("---".equals(line)) {
				headerCount++;
				if (headerCount == 2) {
					builder = new StringBuilder();
					continue;
				}
			} else if (line.startsWith("!")) {
				continue;
			}

			if (line.matches("^-+$")) {
				builder.append("\n");
			}

			line = line
					.replaceAll("\\*\\*([^\\*]+)\\*\\*", "\\\\b{$1}")
					.replaceAll("__([^\\*]+)__", "\\\\i{$1}")
					.replaceAll("^\\s*\\*(\\s)", "\\\\-$1")
					.replaceAll("^\\s*#+\\s+(.+)", "\\\\header{$1}\n")
					.replaceAll("<([^>]+)>", "");

			builder.append(line).append('\n');
		}

		String result = builder.toString().trim();
		result = Pattern.compile("\\[([^]]+)]\\(([^)]+)\\)", Pattern.DOTALL).matcher(result).replaceAll("\\\\url{$2}{$1}");
		return result;
	}
}
