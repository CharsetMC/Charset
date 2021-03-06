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

import com.google.gson.*;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.lib.utils.ThreeState;
import pl.asie.charset.module.tablet.TabletUtil;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiParser extends ParserBase {
	public enum Type {
		MEDIAWIKI,
		DOKUWIKI
	};

	private final Map<String, ThreeState> existsData;
	private ThreeState urlExists(String url) {
		return existsData.getOrDefault(url.toLowerCase(Locale.ROOT), ThreeState.MAYBE);
	}

	private String text = null, displaytitle = null;

	private void initReplacers(Type type) {
		if (type == Type.DOKUWIKI) {
			replacers.add(replace("\\*\\*([^\\*]+)\\*\\*", 0, "\\\\b{$1}"));
			replacers.add(replace("//([^\\*]+)//", 0, "\\\\i{$1}"));
			replacers.add(replace("^\\\\\\\\$", Pattern.MULTILINE, "\\\\nl"));
			replacers.add(replace("\\~\\~(NOTOC)\\~\\~", 0, ""));
		} else {
			replacers.add(replace("'''([^']+)'''", 0, "\\\\b{$1}"));
		}

		replacers.add(replace("''([^']+)''", 0, "\\\\i{$1}"));
		replacers.add(replace("======([^=]+)======", 0,"\\\\header{$1}\n\n"));
		replacers.add(replace("=====([^=]+)=====", 0,"\\\\header{$1}\n\n"));
		replacers.add(replace("====([^=]+)====", 0,"\\\\header{$1}\n\n"));
		replacers.add(replace("===([^=]+)===", 0,"\\\\header{$1}\n\n"));
		replacers.add(replace("===([^=]+)===", 0,"\\\\header{$1}\n\n"));
		replacers.add(replace("==([^=]+)==", 0,"\\\\header{$1}\n\n"));
		replacers.add(replace("=([^=]+)=", 0,"\\\\header{$1}\n\n"));
		replacers.add(replace("\\<s\\>", 0,"\\\\del{"));
		replacers.add(replace("\\<\\/s\\>", 0,"}"));
		replacers.add(replace("^\\*\\*\\*\\*\\*\\*", Pattern.MULTILINE, "\\\\-{5}"));
		replacers.add(replace("^\\*\\*\\*\\*\\*", Pattern.MULTILINE, "\\\\-{4}"));
		replacers.add(replace("^\\*\\*\\*\\*", Pattern.MULTILINE, "\\\\-{3}"));
		replacers.add(replace("^\\*\\*\\*", Pattern.MULTILINE, "\\\\-{2}"));
		replacers.add(replace("^\\*\\*", Pattern.MULTILINE, "\\\\-{1}"));
		replacers.add(replace("^\\s*\\*", Pattern.MULTILINE, "\\\\-"));
		replacers.add(replace("^\\s*\\#", Pattern.MULTILINE, "\\\\-"));

		replacers.add(Pair.of(Pattern.compile("\\[\\[File:([^|.]+\\.[a-z]+)\\|([^|]+)\\|([^|]+)]]", 0), (m) -> {
			if (m.group(2).equals("thumb")) {
				return "\\\\i{Image: " + m.group(3) + "}";
			} else {
				return "\\\\i{Image: " + m.group(2) + "}";
			}
		}));

		replacers.add(Pair.of(Pattern.compile("\\[\\[Image:([^|.]+\\.[a-z]+)\\|([^|]+)\\|([^|]+)\\|([^|]+)]]", 0), (m) -> {
			return "\\\\i{Image: " + m.group(4) + "}";
		}));

		replacers.add(Pair.of(Pattern.compile("\\[\\[([^|\\]]+?)]]", 0), (m) -> {
			try {
				String url = m.group(1);
				if (url.startsWith("Category:") || url.startsWith("Template:")) {
					return "";
				} else if (urlExists(url) != ThreeState.NO) {
					return "\\\\url{/" + TabletUtil.encode(url) + "}{" + m.group(1) + "}";
				} else {
					return "\\\\urlmissing{" + m.group(1) + "}";
				}
			} catch (Exception e) {
				return "???";
			}
		}));

		replacers.add(Pair.of(Pattern.compile("\\[\\[([^|\\]]+?)\\|([^|\\]]+?)]]", 0), (m) -> {
			try {
				String url = m.group(1);
				if (url.startsWith("Category:") || url.startsWith("Template:")) {
					return "";
				} else if (urlExists(url) != ThreeState.NO) {
					return "\\\\url{/" + TabletUtil.encode(url) + "}{" + m.group(2) + "}";
				} else {
					return "\\\\urlmissing{" + m.group(2) + "}";
				}
			} catch (Exception e) {
				return "???";
			}
		}));

		replacers.add(replace("\\<(.+?)\\>", 0, ""));
	}

	private boolean isMcwRedirect = false, isError = false;

	public WikiParser(String string, Type type) {
		existsData = new HashMap<>();
		initReplacers(type);

		if (type == Type.MEDIAWIKI) {
			JsonObject object = new JsonParser().parse(string).getAsJsonObject();
			if (object.has("parse")) {
				object = object.getAsJsonObject("parse");

				if (object.has("iwlinks")) {
					JsonArray array = object.getAsJsonArray("iwlinks");
					for (JsonElement element : array) {
						JsonObject cat = (JsonObject) element;
						if (cat.has("prefix")) {
							String prefix = cat.get("prefix").getAsString();
							if (prefix.equals("mcw")) {
								isMcwRedirect = true;
							}
						}
					}
				}

				if (object.has("links")) {
					JsonArray array = object.getAsJsonArray("links");
					for (JsonElement element : array) {
						JsonObject cat = (JsonObject) element;
						if (cat.has("*")) {
							existsData.put(cat.get("*").getAsString().toLowerCase(Locale.ROOT), cat.has("exists") ? ThreeState.YES : ThreeState.NO);
						}
					}
				}

				if (object.has("wikitext")) {
					JsonObject wikiText = object.getAsJsonObject("wikitext");
					if (wikiText.has("*")) {
						text = wikiText.get("*").getAsString();
					}
				}

				if (object.has("displaytitle")) {
					displaytitle = object.get("displaytitle").getAsString();
				} else if (object.has("title")) {
					displaytitle = object.get("title").getAsString();
				}
			} else if (object.has("error")) {
				isError = true;
				displaytitle = "Error";
				text = object.getAsJsonObject("error").get("info").getAsString();
			}
		} else {
			text = string;
		}
	}

	public boolean isError() {
		return isError;
	}

	public boolean shouldRetain() {
		return !isMcwRedirect;
	}

	public String getText() {
		if (text == null) {
			return null;
		}

		String out = text;
		if (displaytitle != null) {
			out = "\\title{" + displaytitle + "}\n\n" + out;
		}

		for (Pair<Pattern, Function<Matcher, String>> pair : replacers) {
			StringBuffer result = new StringBuffer();
			Matcher matcher = pair.getLeft().matcher(out);
			while (matcher.find()) {
				matcher.appendReplacement(result, pair.getRight().apply(matcher));
			}
			matcher.appendTail(result);
			out = result.toString();
		}

		// remove stray {{..}} segments
		int count = 0;
		int cutStart = 0;
		int cutEnd = 0;
		StringBuilder newOut = new StringBuilder();
		for (int i = 0; i < out.length() - 1; i++) {
			if (out.codePointAt(i) == '{' && (out.codePointAt(i + 1) == '{' || out.codePointAt(i + 1) == '|')) {
				count++;
				if (count == 1) {
					cutStart = i;
					newOut.append(out.substring(cutEnd, cutStart));
				}
			} else if ((out.codePointAt(i) == '}' || out.codePointAt(i) == '|') && out.codePointAt(i + 1) == '}' && count > 0) {
				count--;
				if (count == 0) {
					cutEnd = i + 2;
					if (i < out.length() - 2 && out.codePointAt(i + 2) == '}') {
						cutEnd++;
					}
				}
			}
		}

		newOut.append(out.substring(cutEnd, out.length()));
		return newOut.toString().trim();
	}
}
