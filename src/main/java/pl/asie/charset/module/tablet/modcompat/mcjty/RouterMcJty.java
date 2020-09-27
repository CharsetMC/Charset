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

package pl.asie.charset.module.tablet.modcompat.mcjty;

import pl.asie.charset.lib.utils.UtilProxyCommon;
import pl.asie.charset.module.tablet.format.api.IRouter;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RouterMcJty implements IRouter {
	private final String name, friendlyName;
	private final Map<String, String> langToText = new HashMap<>();

	public RouterMcJty(String name, String friendlyName) {
		this.name = name;
		this.friendlyName = friendlyName;
	}

	protected void add(String lang, String text) {
		String[] parsedText = text.split("\n");
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < parsedText.length; i++) {
			String line = parsedText[i];
			line = line.replaceAll("^\\{b}(.+)$", "\\\\header{$1}\n")
						.replaceAll("^\\{l:(.+)}(.+)$", "\\\\- $2")
						.replaceAll("^\\{/}$", "")
						.replaceAll("^\\s*\\{(.+)}$", "");
			out.append(line).append('\n');
		}

		langToText.put(lang, out.toString().trim());
	}

	@Nullable
	@Override
	public String get(URI path) {
		String text = langToText.get(UtilProxyCommon.proxy.getLanguageCode());
		if (text == null) {
			text = langToText.get("en_us");
			if (text == null) {
				text = "\\header{Error}\nNot found!";
			}
		}
		return text;
	}

	@Override
	public boolean matches(URI path) {
		return "mcjty".equals(path.getScheme()) && name.equals(path.getAuthority());
	}
}
