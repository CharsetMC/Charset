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
