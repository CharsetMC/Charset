/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.gates.render;

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
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;

import pl.asie.charset.lib.utils.RenderUtils;

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

		public IModel getModel(String name) {
			if (modelObjs.containsKey(name)) {
				return modelObjs.get(name);
			}

			IModel model = RenderUtils.getModel(new ResourceLocation(models.get(name)));
			if (model != null) {
				if (model instanceof IRetexturableModel) {
					model = ((IRetexturableModel) model).retexture(ImmutableMap.copyOf(textures));
				}
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
		public String type, texture;
		public Map<String, String> textures;
		public int height = 0;
	}

	public class Torch {
		public final float[] pos = new float[2];
		public String inverter;
	}

	public BaseDefinition base;
	private final Map<String, Definition> definitionMap = new HashMap<String, Definition>();

	public Definition getGateDefinition(ResourceLocation type) {
		return definitionMap.get(type.toString());
	}

	public void load(String baseLoc, Map<String, ResourceLocation> definitions) {
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

			for (String s : definitions.keySet()) {
				try {
					Definition def = GSON.fromJson(new InputStreamReader(
							Minecraft.getMinecraft().getResourceManager().getResource(definitions.get(s)).getInputStream()
					), Definition.class);
					def.init();
					def.merge(base);
					definitionMap.put(s, def);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
