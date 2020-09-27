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

package pl.asie.charset.module.tablet.format.routers;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import pl.asie.charset.ModCharset;
import pl.asie.charset.module.tablet.TabletUtil;
import pl.asie.charset.module.tablet.format.api.IRouterSearchable;
import pl.asie.charset.module.tablet.format.parsers.WikiParser;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collection;

public class RouterMediaWiki implements IRouterSearchable {
	private final String[] hosts;
	private final String name;
	private final String friendlyName;

	public RouterMediaWiki(String name, String friendlyName, String... hosts) {
		this.name = name;
		this.friendlyName = friendlyName;
		this.hosts = hosts;
	}

	@Nullable
	@Override
	public String get(URI path) {
		String err = "";

		String[] hostsChecks = hosts;

		if ("gamepedia".equals(name) && path.toString().endsWith("%20%28Vanilla%29")) {
			try {
				path = new URI(path.toString().replaceAll("%20%28Vanilla%29", ""));
				hostsChecks = new String[]{"minecraft.gamepedia.com"};
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (String host : hostsChecks) {
			try {
				URI uri = new URI("http://" + host + "/api.php?action=parse&prop=wikitext%7clanglinks%7ccategories%7clinks%7cdisplaytitle%7ciwlinks%7cproperties"
						+ "&format=json&page=" + TabletUtil.encode(path.getPath().substring(1)));

				HttpClient client = TabletUtil.createHttpClient();
				HttpGet request = new HttpGet(uri);

				HttpResponse response = client.execute(request);
				if (response.getStatusLine().getStatusCode() == 200) {
					WikiParser mediaWikiData = new WikiParser(
							new String(ByteStreams.toByteArray(response.getEntity().getContent()), Charsets.UTF_8),
							WikiParser.Type.MEDIAWIKI
					);
					if (mediaWikiData.shouldRetain()) {
						if (mediaWikiData.isError()) {
							err = err + mediaWikiData.getText() + "\n\n";
						} else {
							return mediaWikiData.getText();
						}
					}
				} else {
					err = err + "ERROR: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() + "\n\n";
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return err.trim();
	}

	@Override
	public boolean matches(URI path) {
		return "wiki".equals(path.getScheme()) && name.equals(path.getHost());
	}

	@Override
	public void find(Collection<SearchResult> results, String query) {
		for (String host : hosts) {
			try {
				URI uri = new URI("https://" + host + "/api.php?action=opensearch&search=" +
						TabletUtil.encode(query)
						+ "&limit=50&format=json");

				HttpClient client = TabletUtil.createHttpClient();
				HttpGet request = new HttpGet(uri);

				HttpResponse response = client.execute(request);
				if (response.getStatusLine().getStatusCode() == 200) {
					String data = new String(ByteStreams.toByteArray(response.getEntity().getContent()), Charsets.UTF_8);
					JsonArray array = new JsonParser().parse(data).getAsJsonArray();
					if (array.size() == 4) {
						JsonArray names = array.get(1).getAsJsonArray();
						JsonArray urls = array.get(3).getAsJsonArray();
						for (int i = 0; i < Math.min(names.size(), urls.size()); i++) {
							String name = names.get(i).getAsString();
							URI realUri = new URI(urls.get(i).getAsString());
							if (!name.contains("/")) {
								SearchResult result = new SearchResult(name, friendlyName,
										new URI("wiki://" + this.name + realUri.getPath()));
								results.add(result);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
