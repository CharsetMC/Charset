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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouterDokuWiki implements IRouterSearchable {
	private static final Pattern PATTERN = Pattern.compile("<a href=\"[^\"]+\" class=\"wikilink1\" title=\"([:_a-z]+)\">([^<]+)</a>");

	private final String host;
	private final String name;
	private final String friendlyName;

	public RouterDokuWiki(String name, String friendlyName, String host) {
		this.name = name;
		this.friendlyName = friendlyName;
		this.host = host;
	}

	@Nullable
	@Override
	public String get(URI path) {
		String err = "";

		try {
			URI uri = new URI("http://" + host + "?id=" + TabletUtil.encode(path.getPath().substring(1).replace('/', ':')) + "&do=export_raw");
			HttpClient client = TabletUtil.createHttpClient();
			HttpGet request = new HttpGet(uri);

			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				WikiParser mediaWikiData = new WikiParser(
						new String(ByteStreams.toByteArray(response.getEntity().getContent()), Charsets.UTF_8),
						WikiParser.Type.DOKUWIKI
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

		return err.trim();
	}

	@Override
	public boolean matches(URI path) {
		return "wiki".equals(path.getScheme()) && name.equals(path.getHost());
	}

	@Override
	public void find(Collection<SearchResult> results, String query) {
		try {
			URI uri = new URI("http://" + host + "?do=search&id=" + TabletUtil.encode(query));

			HttpClient client = TabletUtil.createHttpClient();
			HttpGet request = new HttpGet(uri);

			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				// No API? No way!
				String data = new String(ByteStreams.toByteArray(response.getEntity().getContent()), Charsets.UTF_8);
				int i = data.indexOf("search_results");
				if (i >= 0) {
					Matcher m = PATTERN.matcher(data.substring(i));
					while (m.find()) {
						SearchResult result = new SearchResult(m.group(2), friendlyName,
								new URI("wiki://" + this.name + "/" + m.group(1).replace(':', '/')));
						results.add(result);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
