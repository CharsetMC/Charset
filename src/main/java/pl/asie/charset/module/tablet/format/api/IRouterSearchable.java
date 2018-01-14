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

package pl.asie.charset.module.tablet.format.api;

import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collection;
import java.util.List;

public interface IRouterSearchable extends IRouter {
	class SearchResult {
		public final String text;
		public final String providerName;
		public final URI uri;

		public SearchResult(String text, String providerName, URI uri) {
			this.text = text;
			this.providerName = providerName;
			this.uri = uri;
		}

		@Override
		public int hashCode() {
			return 31 * text.hashCode() + uri.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof SearchResult)) {
				return false;
			} else {
				SearchResult other = (SearchResult) o;
				return other.text.equals(text) && other.uri.equals(uri);
			}
		}
	}

	void find(Collection<SearchResult> results, String query);
}
