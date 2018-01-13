/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import pl.asie.charset.module.tablet.format.api.IRouter;
import pl.asie.charset.module.tablet.format.api.IRouterSearchable;
import pl.asie.charset.module.tablet.format.api.TabletAPI;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RouterSearch implements IRouter {
	private final Cache<String, List<IRouterSearchable.SearchResult>> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(30, TimeUnit.MINUTES)
			.build();

	@Nullable
	@Override
	public String get(URI path) {
		try {
			String query = URLDecoder.decode(path.getPath(), Charsets.UTF_8.name()).substring(1);
			List<IRouterSearchable.SearchResult> resultList = cache.getIfPresent(query.toLowerCase());
			if (resultList == null) {
				resultList = new ArrayList<>();
				for (IRouter router : TabletAPI.INSTANCE.getRouters()) {
					if (router instanceof IRouterSearchable) {
						((IRouterSearchable) router).find(resultList, query.toLowerCase(Locale.ROOT));
					}
				}
				cache.put(query.toLowerCase(Locale.ROOT), resultList);
			}

			StringBuilder search = new StringBuilder("\\title{" + query + "}\n\n");
			if (resultList.size() == 0) {
				search.append("No results.");
			} else {
				Set<IRouterSearchable.SearchResult> resultSet = new HashSet<>();
				for (IRouterSearchable.SearchResult result : resultList) {
					if (resultSet.add(result)) {
						search.append("\\- \\url{" + result.uri.toString() + "}{" + result.text + "} (" + result.providerName + ")\n");
					}
				}

				search.append("\n" + resultSet.size() + " result" + (resultSet.size() == 1 ? "" : "s") + ".");
			}

			return search.toString().trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public boolean matches(URI path) {
		return "about".equals(path.getScheme()) && "search".equals(path.getHost());
	}
}
