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

package pl.asie.charset.lib.resources;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import pl.asie.charset.ModCharset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ColorPaletteParser {
	public static final ColorPaletteParser INSTANCE = new ColorPaletteParser();
	private final Gson gson = new Gson();
	private final ResourceLocation COLOR_PALETTE_LOC = new ResourceLocation("charset", "color_palette.json");
	private Data data;

	private static class Data {
		public int version;
		public Map<String, Map<String, double[]>> palettes;

		public void add(Data other) {
			for (String s : other.palettes.keySet()) {
				if (!palettes.containsKey(s)) {
					palettes.put(s, other.palettes.get(s));
				} else {
					Map<String, double[]> parent = palettes.get(s);
					for (Map.Entry<String, double[]> entry : other.palettes.get(s).entrySet()) {
						parent.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
	}

	private ColorPaletteParser() {

	}

	public boolean hasColor(String namespace, String color) {
		return data.palettes.containsKey(namespace) && data.palettes.get(namespace).containsKey(color);
	}

	public double[] getColor(String namespace, String color) {
		if (hasColor(namespace, color)) {
			return data.palettes.get(namespace).get(color);
		} else {
			return new double[] { 0.0, 0.0, 0.0, 0.0 }; // Fallback
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		this.data = null;

		try {
			for (IResource resource : Minecraft.getMinecraft().getResourceManager().getAllResources(COLOR_PALETTE_LOC)) {
				Data data;
				String resourcePackName = resource.getResourcePackName();

				try (InputStream stream = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(stream)) {
					data = gson.fromJson(reader, Data.class);
				} finally {
					IOUtils.closeQuietly(resource);
				}

				if (data == null) {
					ModCharset.logger.warn("Could not parse color_palette.json found in " + resourcePackName + " - not loaded.");
					continue;
				} else if (data.version != 1) {
					ModCharset.logger.warn("Unsupported version of color_palette.json found in " + resourcePackName + " - not loaded.");
					continue;
				}

				if (this.data == null) {
					this.data = data;
 				} else {
					this.data.add(data);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		MinecraftForge.EVENT_BUS.post(new ColorPaletteUpdateEvent(this));
	}
}
