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

package pl.asie.simplelogic.gates.render;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;

import pl.asie.charset.lib.utils.RenderUtils;

// TODO: Rewrite - remove distinction between BaseDefinition and Definition, allow multiple InputStream for resource
// TODO: pack overlays
public class GateRenderDefinitions {
	public static final GateRenderDefinitions INSTANCE = new GateRenderDefinitions();
	private static final Gson GSON = new GsonBuilder().create();

	public class BaseDefinition {
		public Map<String, Integer> colorMul;

		private Map<String, String> colors = new HashMap<String, String>();
		private Map<String, String> models;
		private Map<String, String> textures;

		public void init() {
			if (models == null) {
				models = new HashMap<String, String>();
			}
			if (textures == null) {
				textures = new HashMap<String, String>();
			}

			colorMul = new HashMap<String, Integer>();

			for (String s : colors.keySet()) {
				int c = Integer.parseInt(colors.get(s), 16);
				colorMul.put(s, 0xFF000000 | (c & 0x00FF00) | ((c & 0xFF0000) >> 16) | ((c & 0x0000FF) << 16));
			}
		}

		public ResourceLocation getTexture(String name) {
			return textures.containsKey(name) ? new ResourceLocation(textures.get(name)) : TextureMap.LOCATION_MISSING_TEXTURE;
		}
	}

	public class Definition {
		public List<Layer> layers = new ArrayList<Layer>();
		public List<Torch> torches = new ArrayList<Torch>();

		private Map<String, String> models;
		private Map<String, IModel> modelObjs;
		private Map<String, String> textures;

		public void init() {
			modelObjs = new HashMap<String, IModel>();
			if (models == null) {
				models = new HashMap<String, String>();
			}
			if (textures == null) {
				textures = new HashMap<String, String>();
			}
		}

		public void postInit() {
			for (Layer layer : layers) {
				if (layer.texture == null && layer.color_mask != null) {
					layer.texture = textures.get("top");
				}
				layer.textureBase = layer.texture;
			}
		}

		public IModel getModel(String name) {
			if (modelObjs.containsKey(name)) {
				return modelObjs.get(name);
			}

			IModel model = RenderUtils.getModel(new ResourceLocation(models.get(name)));
			if (model != null) {
				model = model.retexture(ImmutableMap.copyOf(textures));
				modelObjs.put(name, model);
			}
			return model;
		}

		public List<IModel> getAllModels() {
			List<IModel> modelList = new ArrayList<IModel>();
			for (String s : models.keySet()) {
				IModel model = getModel(s);
				if (model != null) {
					modelList.add(model);
				}
			}
			return modelList;
		}

		public void merge(BaseDefinition baseDef) {
			for (String s : baseDef.models.keySet()) {
				if (!models.containsKey(s)) {
					models.put(s, baseDef.models.get(s));
				}
			}
			for (String s : baseDef.textures.keySet()) {
				if (!textures.containsKey(s)) {
					textures.put(s, baseDef.textures.get(s));
				}
			}
		}
	}

	public class Layer {
		public String type, texture, textureBase, color_mask;
		public Map<String, String> textures;
		public int height = 0;
	}

	public class Torch {
		public final float[] pos = new float[2];
		public String color_on, color_off;
		public String model_on, model_off;
		public String inverter;
	}

	public BaseDefinition base;
	private final Map<ResourceLocation, Definition> definitionMap = new HashMap<>();

	public Definition getGateDefinition(ResourceLocation type) {
		return definitionMap.get(type);
	}

	public void load(String baseLoc, Map<ResourceLocation, ResourceLocation> definitions) {
		try {
			base = GSON.fromJson(new InputStreamReader(
					Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(baseLoc)).getInputStream()
			), BaseDefinition.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		definitionMap.clear();

		if (base != null) {
			base.init();

			for (ResourceLocation s : definitions.keySet()) {
				try {
					Definition def = GSON.fromJson(new InputStreamReader(
							Minecraft.getMinecraft().getResourceManager().getResource(definitions.get(s)).getInputStream()
					), Definition.class);
					def.init();
					def.merge(base);
					def.postInit();
					definitionMap.put(s, def);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
