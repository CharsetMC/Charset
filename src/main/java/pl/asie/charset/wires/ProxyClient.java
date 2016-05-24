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

package pl.asie.charset.wires;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import pl.asie.charset.api.wires.WireType;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.wires.logic.PartWireBase;
import pl.asie.charset.wires.render.RendererWire;

public class ProxyClient extends ProxyCommon {
	public static RendererWire rendererWire = new RendererWire();

	@Override
	public void drawWireHighlight(PartWireBase wire) {
		int lineMaskCenter = 0xFFF;
		EnumFacing[] faces = WireUtils.getConnectionsForRender(wire.location);
		for (int i = 0; i < faces.length; i++) {
			EnumFacing face = faces[i];
			if (wire.connectsAny(face)) {
				int lineMask = 0xfff;
				lineMask &= ~RenderUtils.getLineMask(face.getOpposite());
				RenderUtils.drawSelectionBoundingBox(wire.getSelectionBox(i + 1), lineMask);
				lineMaskCenter &= ~RenderUtils.getLineMask(face);
			}
		}
		if (lineMaskCenter != 0) {
			RenderUtils.drawSelectionBoundingBox(wire.getSelectionBox(0), lineMaskCenter);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charsetwires:wire", "multipart"), rendererWire);
		event.getModelRegistry().putObject(new ModelResourceLocation("charsetwires:wire", "inventory"), rendererWire);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		for (WireType type : WireType.values()) {
			rendererWire.registerSheet(event.getMap(), type, new ResourceLocation("charsetwires", "blocks/wire_" + type.name().toLowerCase()));
		}
	}
}
