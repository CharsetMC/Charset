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

package pl.asie.charset.lib.utils;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.recipe.IOutputSupplier;
import pl.asie.charset.lib.recipe.IOutputSupplierFactory;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ModPathIterator {
	private ModPathIterator() {

	}

	public static Collection<Pair<String, Path>> getValidPaths(String prefix) {
		List<Pair<String, Path>> paths = new ArrayList<>();
		for (ModContainer container : Loader.instance().getActiveModList()) {
			File file = container.getSource();
			try {
				if (file.exists()) {
					if (file.isDirectory()) {
						File f = new File(file, prefix.replaceAll("%1", container.getModId()));
						if (f.exists()) {
							paths.add(Pair.of(container.getModId(), f.toPath()));
						}
					} else {
						FileSystem fileSystem = FileSystems.newFileSystem(file.toPath(), null);
						Path p = fileSystem.getPath(prefix.replaceAll("%1", container.getModId()));
						if (Files.exists(p)) {
							paths.add(Pair.of(container.getModId(), p));
						}
					}
				}
			} catch (NoSuchFileException | FileSystemNotFoundException e) {
				// Don't worry~
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paths;
	}
}
