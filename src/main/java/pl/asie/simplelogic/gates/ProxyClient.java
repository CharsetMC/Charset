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

package pl.asie.simplelogic.gates;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.lib.render.sprite.PixelOperationSprite;
import pl.asie.simplelogic.gates.render.GateRenderDefinitions;
import pl.asie.simplelogic.gates.render.RendererGate;
import pl.asie.charset.lib.utils.RegistryUtils;

public class ProxyClient extends ProxyCommon {
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelRegister(ModelRegistryEvent event) {
		RegistryUtils.registerModel(SimpleLogicGates.itemGate, 0, "charset:logic_gate");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:logic_gate", "normal"), RendererGate.INSTANCE);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:logic_gate", "inventory"), RendererGate.INSTANCE);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		GateRenderDefinitions.INSTANCE.load("simplelogic:gatedefs/base.json", SimpleLogicGates.logicDefinitions);

		for (ResourceLocation rs : SimpleLogicGates.logicClasses.keySet()) {
			Set<ResourceLocation> textures = new HashSet<>();
			Map<String, TIntObjectMap<String>> colorMasks = new HashMap<>();

			// step 1: gather colormasks
			int i = 0;
			for (GateRenderDefinitions.Layer layer : GateRenderDefinitions.INSTANCE.getGateDefinition(rs).layers) {
				if (layer.color_mask != null) {
					layer.texture = rs.getResourceDomain() + ":blocks/" + rs.getResourcePath() + "/layer_" + i;
					colorMasks.computeIfAbsent(layer.textureBase, (k) -> new TIntObjectHashMap<>())
						.put(Integer.parseInt(layer.color_mask, 16), layer.texture);
				}
				i++;
			}

			// step 2: gather textures
			GateRenderDefinitions.Definition def = GateRenderDefinitions.INSTANCE.getGateDefinition(rs);
			for (IModel model : def.getAllModels()) {
				textures.addAll(model.getTextures());
			}

			for (GateRenderDefinitions.Layer layer : GateRenderDefinitions.INSTANCE.getGateDefinition(rs).layers) {
				if (layer.texture != null) {
					textures.add(new ResourceLocation(layer.texture));
				}
			}

			// step 3: add colormasked textures
			for (String baseTexture : colorMasks.keySet()) {
				TIntObjectMap<String> resultingTextures = colorMasks.get(baseTexture);
				resultingTextures.forEachEntry((color, resultingTexture) -> {
					event.getMap().setTextureEntry(new PixelOperationSprite(resultingTexture, new ResourceLocation(baseTexture),
							((getter, x, y, value) -> (value & 0xFFFFFF) == color ? -1 : 0)));
					textures.remove(new ResourceLocation(resultingTexture));
					return true;
				});
				event.getMap().setTextureEntry(new PixelOperationSprite(baseTexture, new ResourceLocation(baseTexture),
						(((getter, x, y, value) -> {
							if (resultingTextures.containsKey(value & 0xFFFFFF)) {
								return 0;
							} else {
								return value;
							}
						}))));
				textures.remove(new ResourceLocation(baseTexture));
			}

			// step 4: add non-colormasked textures
			for (ResourceLocation location : textures) {
				event.getMap().registerSprite(location);
			}
		}
	}
}
