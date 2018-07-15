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

package pl.asie.charset.module.tablet.modcompat.guideapi;

import amerifrance.guideapi.api.IPage;
import amerifrance.guideapi.api.impl.Book;
import amerifrance.guideapi.api.impl.abstraction.CategoryAbstract;
import amerifrance.guideapi.api.impl.abstraction.EntryAbstract;
import amerifrance.guideapi.page.PageText;
import amerifrance.guideapi.page.PageTextImage;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.module.tablet.TabletUtil;
import pl.asie.charset.module.tablet.format.api.IRouter;
import pl.asie.charset.module.tablet.format.api.IRouterSearchable;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RouterGuideAPI implements IRouterSearchable {
	private final ResourceLocation location;
	private final Book book;

	public RouterGuideAPI(ResourceLocation location, Book book) {
		this.location = location;
		this.book = book;
	}

	public CategoryAbstract getCategory(String name) {
		name = name.toLowerCase(Locale.ROOT);
		for (CategoryAbstract category : book.getCategoryList()) {
			if (category.name.toLowerCase(Locale.ROOT).equals(name)) {
				return category;
			}
		}
		return null;
	}

	private String getIndex() {
		StringBuilder builder = new StringBuilder("\\title{\\local{" + book.getDisplayName() + "}}\n");
		for (CategoryAbstract category : book.getCategoryList()) {
			builder.append("\n\\- \\url{/" + location.getPath() + "/" + TabletUtil.encode(category.name.toLowerCase(Locale.ROOT)) + "}{" + category.getLocalizedName() + "}");
		}
		return builder.toString();
	}

	private String getCategory(CategoryAbstract category) {
		StringBuilder builder = new StringBuilder("\\title{" + category.getLocalizedName() + "}\n");
		for (Map.Entry<ResourceLocation, EntryAbstract> entry : category.entries.entrySet()) {
			builder.append("\n\\- \\url{/" + location.getPath() + "/" + TabletUtil.encode(category.name.toLowerCase(Locale.ROOT)) + "/" + TabletUtil.encode(entry.getValue().name.toLowerCase(Locale.ROOT)) + "}{" + entry.getValue().getLocalizedName() + "}");
		}
		builder.append("\n\n\\url{/" + location.getPath() + "/index}{Back}");
		return builder.toString();
	}

	private String getEntry(CategoryAbstract category, EntryAbstract entry) {
		StringBuilder builder = new StringBuilder("\\title{" + entry.getLocalizedName() + "}\n\n");
		for (IPage page : entry.pageList) {
			if (page instanceof PageTextImage) {
				builder.append(((PageTextImage) page).draw.replaceAll("\\\\n", "\n")).append("\n\n");
			} else if (page instanceof PageText) {
				builder.append(((PageText) page).draw.replaceAll("\\\\n", "\n")).append("\n\n");
			}
		}
		builder.append("\\url{/" + location.getPath() + "/" + TabletUtil.encode(category.name.toLowerCase(Locale.ROOT)) + "}{Back}");
		return builder.toString().trim();
	}

	@Nullable
	@Override
	public String get(URI path) {
		String cutPath = TabletUtil.decode(path.getPath().substring(location.getPath().length() + 1));
		if (cutPath.length() <= 1 || "/index".equals(cutPath)) {
			return getIndex();
		} else {
			if (cutPath.startsWith("/")) cutPath = cutPath.substring(1);
			if (cutPath.endsWith("/")) cutPath = cutPath.substring(cutPath.length() - 1);
			if (cutPath.endsWith("/index")) cutPath = cutPath.substring(cutPath.length() - 6);

			String[] splits = cutPath.split("/");
			if (splits.length == 1) {
				return getCategory(getCategory(splits[0]));
			} else if (splits.length == 2) {
				CategoryAbstract category = getCategory(splits[0]);
				for (Map.Entry<ResourceLocation, EntryAbstract> entry : category.entries.entrySet()) {
					if (splits[1].equals(entry.getValue().name.toLowerCase(Locale.ROOT))) {
						return getEntry(category, entry.getValue());
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean matches(URI path) {
		return "guideapi".equals(path.getScheme()) && location.getNamespace().equals(path.getHost())
				&& path.getPath().startsWith("/" + location.getPath());
	}

	@Override
	public void find(Collection<SearchResult> results, String query) {
		for (CategoryAbstract category : book.getCategoryList()) {
			for (Map.Entry<ResourceLocation, EntryAbstract> entry : category.entries.entrySet()) {
				if (query.toLowerCase().contains(entry.getValue().getLocalizedName().toLowerCase())) {
					try {
						results.add(new SearchResult(
								entry.getValue().getLocalizedName(),
								book.getLocalizedDisplayName(),
								new URI("guideapi://" + location.getNamespace() + "/" + location.getPath() + "/" + TabletUtil.encode(category.name.toLowerCase(Locale.ROOT)) + "/" + TabletUtil.encode(entry.getValue().name.toLowerCase(Locale.ROOT)))
						));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
