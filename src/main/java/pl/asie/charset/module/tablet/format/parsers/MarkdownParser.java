/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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
