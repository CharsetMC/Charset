/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.module.tablet.format.parsers;

import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.lib.utils.ThreeState;
import pl.asie.charset.module.tablet.TabletUtil;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser extends ParserBase {
	public MarkdownParser() {
		Pattern.compile("<!--(.+)-->");
		replacers.add(Pair.of(Pattern.compile("\\[([^]]*)]\\(([^)]+)\\)", Pattern.DOTALL), this::urlHandler));
		replace("<!--(.+)-->", Pattern.DOTALL | Pattern.MULTILINE, "");
	}

	protected String urlHandler(Matcher m) {
		return "\\\\urlmissing{" + m.group(1) + "}";
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
			}

			while (line.startsWith("!") && line.contains(")")) {
				line = line.substring(line.indexOf(")") + 1).trim() + "\n";
			}

			if (line.matches("^-+$")) {
				builder.append("\n");
			}

			line = line
					.replaceAll("\\\\", "\\\\")
					.replaceAll("\\*\\*([^\\*]+)\\*\\*", "\\\\b{$1}")
					.replaceAll("__([^\\*]+)__", "\\\\i{$1}")
					.replaceAll("^(\\s*)\\*(\\s)", "$1\\\\-$2")
					.replaceAll("^(\\s*)\\+(\\s)", "$1\\\\-$2")
					.replaceAll("^(\\s*)-(\\s)", "$1\\\\-$2")
					.replaceAll("^\\s*#+\\s+([^#]+)\\s*#*", "\\\\header{$1}\n")
					.replaceAll("<([^>]+)>", "");

			builder.append(line).append('\n');
		}

		String out = builder.toString().trim();
		for (Pair<Pattern, Function<Matcher, String>> pair : replacers) {
			StringBuffer result = new StringBuffer();
			Matcher matcher = pair.getLeft().matcher(out);
			while (matcher.find()) {
				matcher.appendReplacement(result, pair.getRight().apply(matcher));
			}
			matcher.appendTail(result);
			out = result.toString();
		}

		return out;
	}
}
