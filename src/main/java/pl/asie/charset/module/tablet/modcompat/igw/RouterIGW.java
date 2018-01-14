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

package pl.asie.charset.module.tablet.modcompat.igw;

import com.google.common.base.Charsets;
import com.google.common.io.MoreFiles;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import pl.asie.charset.ModCharset;
import pl.asie.charset.module.tablet.format.api.IRouterSearchable;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouterIGW implements IRouterSearchable {
	private static final Pattern pattern = Pattern.compile("\\[([a-z]+)\\{([^}\\]]*)}]");
	private final String modid, friendlyName;
	private final Path path;

	public String getFriendlyName() {
		return friendlyName;
	}

	public RouterIGW(String modid, Path path) {
		this.modid = modid;
		this.path = path;

		String fn = modid;
		for (ModContainer container : Loader.instance().getActiveModList()) {
			if (container.getModId().equals(modid)) {
				fn = container.getName();
				break;
			}
		}
		this.friendlyName = fn + " In-Game Wiki";
	}

	public String getItem(String name) {
		ResourceLocation loc = new ResourceLocation(modid, name);
		Path p = getDocIfExists(loc);
		if (p != null) {
			try {
				Item i = Item.getByNameOrId(loc.toString());
				byte[] data = MoreFiles.asByteSource(p).read();
				String s = new String(data, Charsets.UTF_8);
				s = s.replaceAll("[0-9+]\\. ", "\n\\- ");

				StringBuffer result = new StringBuffer();
				Matcher matcher = pattern.matcher(s);
				while (matcher.find()) {
					String cmd = matcher.group(1);
					String[] args = matcher.group(2).split(", ");
					if ("link".equals(cmd) && args.length <= 1) {
						if (args.length <= 0 || args[0].length() == 0) {
							matcher.appendReplacement(result, "}");
						} else {
							ResourceLocation loc2 = new ResourceLocation(args[0]);
							String url = "igw://" + loc2.getResourceDomain() + "/item/" + loc2.getResourcePath().replaceAll("^block/", "").replaceAll("^item/", "");
							matcher.appendReplacement(result, "\\\\url{" + url + "}{");
						}
					} else if ("prefix".equals(cmd) && args.length <= 1) {
						if (args.length <= 0 || args[0].length() == 0) {
							matcher.appendReplacement(result, "}\n");
						} else {
							String cmdn = "header";
							if ("l".equals(args[0])) cmdn = "b";
							else if ("m".equals(args[0])) cmdn = "del";
							else if ("n".equals(args[0])) cmdn = "u";
							else if ("o".equals(args[0])) cmdn = "i";

							matcher.appendReplacement(result, "\\\\" + cmdn + "{");
						}
					} else if ("image".equals(cmd) && args.length == 4) {
						matcher.appendReplacement(result, "\\\\img{" + args[3] + "}{" + args[2] + "}");
					} else {
						ModCharset.logger.warn("Unsupported IGW command " + cmd + "(" + args.length + ")!");
						matcher.appendReplacement(result, "");
					}
				}
				matcher.appendTail(result);
				String out = result.toString().trim();
				if (i != null) {
					String unl = i.getUnlocalizedName() + ".name";
					if (I18n.canTranslate(unl)) {
						out = "\\title{" + I18n.translateToLocal(unl) + "}\n\n" + out;
					}
				}
				return out;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

	public String getIndex() {
		StringBuilder builder = new StringBuilder("\\title{" + friendlyName + "}\n");

		for (Item i : Item.REGISTRY) {
			if (modid.equals(i.getRegistryName().getResourceDomain())) {
				Path p = getDocIfExists(i.getRegistryName());
				if (p != null) {
					String nname = I18n.translateToLocal(i.getUnlocalizedName() + ".name");
					builder.append("\n\\- \\url{/item/" + i.getRegistryName().getResourcePath() + "}{" + nname + "}");
				}
			}
		}

		return builder.toString();
	}

	@Nullable
	@Override
	public String get(URI path) {
		if ("item".equals(path.getScheme())) {
			return getItem(path.getPath().substring(1));
		} else if ("igw".equals(path.getScheme())) {
			if (path.getPath().startsWith("/item/")) {
				return getItem(path.getPath().substring(6));
			} else if (path.getPath().length() <= 1 || path.getPath().startsWith("/index")) {
				return getIndex();
			}
		}

		return null;
	}

	@Nullable
	private Path getDocIfExists(ResourceLocation loc, String langCode, String b) {
		Path nPath = path.resolve("./" + langCode + "/" + b + "/" + loc.getResourcePath() + ".txt");
		if (Files.exists(nPath)) {
			return nPath;
		} else {
			return null;
		}
	}

	@Nullable
	public Path getDocIfExists(ResourceLocation loc) {
		String l = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();
		Path path = getDocIfExists(loc, l, "block");
		if (path == null) path = getDocIfExists(loc, l, "item");
		if (path == null && !("en_us".equals(l))) {
			path = getDocIfExists(loc, "en_us", "block");
			if (path == null) path = getDocIfExists(loc, "en_us", "item");
		}
		return path;
	}

	@Override
	public boolean matches(URI path) {
		if ("igw".equals(path.getScheme())) {
			return path.getHost().equals(modid);
		} else if ("item".equals(path.getScheme()) && !("minecraft".equals(modid))) {
			return path.getHost().equals(modid);
		} else {
			return false;
		}
	}

	@Override
	public void find(Collection<SearchResult> results, String query) {
		for (Item i : Item.REGISTRY) {
			if (modid.equals(i.getRegistryName().getResourceDomain())) {
				Path p = getDocIfExists(i.getRegistryName());
				if (p != null) {
					try {
						String name = I18n.translateToLocal(i.getUnlocalizedName() + ".name");
						if (query.toLowerCase().contains(name.toLowerCase())) {
							results.add(new SearchResult(
									name,
									friendlyName,
									new URI("igw://" + modid + "/item/" + i.getRegistryName().getResourcePath())
							));
						}
					} catch (Exception e) {
						// Skip
					}
				}
			}
		}
	}
}
