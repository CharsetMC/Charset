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

package pl.asie.charset.module.tablet.format.routers;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.util.text.translation.I18n;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.module.tablet.format.api.IRouter;
import pl.asie.charset.module.tablet.format.api.IRouterSearchable;
import pl.asie.charset.module.tablet.format.api.TabletAPI;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RouterIndex implements IRouter {
	@Nullable
	@Override
	public String get(URI path) {
		try {
			StringBuilder result = new StringBuilder("\\title{Tablet}\n\n");
			for (Pair<String, String> book : TabletAPI.INSTANCE.getBooks()) {
				result.append("\\- \\url{" + book.getRight() + "}{" + I18n.translateToLocal(book.getLeft()) + "}\n");
			}

			return result.toString().trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public boolean matches(URI path) {
		return "about".equals(path.getScheme()) && "index".equals(path.getHost());
	}
}
