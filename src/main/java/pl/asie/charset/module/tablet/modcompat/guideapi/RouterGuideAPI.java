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
			builder.append("\n\\- \\url{/" + location.getResourcePath() + "/" + TabletUtil.encode(category.name.toLowerCase(Locale.ROOT)) + "}{" + category.getLocalizedName() + "}");
		}
		return builder.toString();
	}

	private String getCategory(CategoryAbstract category) {
		StringBuilder builder = new StringBuilder("\\title{" + category.getLocalizedName() + "}\n");
		for (Map.Entry<ResourceLocation, EntryAbstract> entry : category.entries.entrySet()) {
			builder.append("\n\\- \\url{/" + location.getResourcePath() + "/" + TabletUtil.encode(category.name.toLowerCase(Locale.ROOT)) + "/" + TabletUtil.encode(entry.getValue().name.toLowerCase(Locale.ROOT)) + "}{" + entry.getValue().getLocalizedName() + "}");
		}
		builder.append("\n\n\\url{/" + location.getResourcePath() + "/index}{Back}");
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
		builder.append("\\url{/" + location.getResourcePath() + "/" + TabletUtil.encode(category.name.toLowerCase(Locale.ROOT)) + "}{Back}");
		return builder.toString().trim();
	}

	@Nullable
	@Override
	public String get(URI path) {
		String cutPath = TabletUtil.decode(path.getPath().substring(location.getResourcePath().length() + 1));
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
		return "guideapi".equals(path.getScheme()) && location.getResourceDomain().equals(path.getHost())
				&& path.getPath().startsWith("/" + location.getResourcePath());
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
								new URI("guideapi://" + location.getResourceDomain() + "/" + location.getResourcePath() + "/" + TabletUtil.encode(category.name.toLowerCase(Locale.ROOT)) + "/" + TabletUtil.encode(entry.getValue().name.toLowerCase(Locale.ROOT)))
						));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
