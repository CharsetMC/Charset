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

import com.google.common.base.Charsets;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.MoreFiles;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.ModPathIterator;
import pl.asie.charset.module.tablet.format.api.TabletAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@CharsetModule(
		name = "mcjtylib:tablet",
		profile = ModuleProfile.COMPAT,
		dependencies = {"tablet"}
)
public class CharsetTabletCompatMcJty {
	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPostInit(FMLPostInitializationEvent event) {
		Table<String, String, Path> books = HashBasedTable.create();

		// HAAAYO
		for (Pair<String, Path> pair : ModPathIterator.getValidPaths("assets/%1/text/")) {
			Path p = pair.getRight();
			try {
				Iterator<Path> it = Files.walk(p).iterator();
				while (it.hasNext()) {
					Path path = it.next();
					String s = path.getFileName().toString();
					if (s.startsWith("manual") && s.endsWith(".txt")) {
						String name = s.substring(0, s.length() - 4);
						String lang = "en_us";
						if (name.lastIndexOf('-') >= 0) {
							lang = name.substring(name.lastIndexOf('-') + 1);
							name = name.substring(0, name.lastIndexOf('-'));
						}

						books.put(pair.getLeft() + "_" + name, lang, path);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (String name : books.rowKeySet()) {
			Map<String, String> texts = new HashMap<>();
			String friendlyName = null;

			for (String lang : books.row(name).keySet()) {
				try {
					byte[] data = MoreFiles.asByteSource(books.get(name, lang)).read();
					String text = new String(data, Charsets.UTF_8);
					if (lang.equals("en_us")) {
						String firstLine = text.split("\n", 2)[0];
						friendlyName = firstLine.substring(firstLine.lastIndexOf('}') + 1);
					}
					texts.put(lang, text);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (friendlyName != null) {
				RouterMcJty routerMcJty = new RouterMcJty(name, friendlyName);
				for (String lang : texts.keySet()) {
					routerMcJty.add(lang, texts.get(lang));
				}

				TabletAPI.INSTANCE.registerRouter(routerMcJty);
				TabletAPI.INSTANCE.addBook(friendlyName, "mcjty://" + name + "/");
			}
		}
	}
}
