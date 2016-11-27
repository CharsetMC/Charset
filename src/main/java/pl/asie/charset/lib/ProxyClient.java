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

package pl.asie.charset.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;
import pl.asie.charset.lib.audio.manager.AudioStreamManagerClient;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.render.ModelFactory;
import pl.asie.charset.lib.utils.RenderUtils;

public class ProxyClient extends ProxyCommon {
	// TODO 1.11
	/*
	public static final RendererWire rendererWire = new RendererWire();

	@Override
	public void drawWireHighlight(PartWire wire) {
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
	*/

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
	//	event.getModelRegistry().putObject(new ModelResourceLocation("charsetlib:wire", "multipart"), rendererWire);
	//	event.getModelRegistry().putObject(new ModelResourceLocation("charsetlib:wire", "inventory"), rendererWire);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		ModelFactory.clearCaches();
		ColorLookupHandler.INSTANCE.clear();

	//	for (WireFactory factory : WireManager.REGISTRY.getValues()) {
	//		rendererWire.registerSheet(event.getMap(), factory);
	//	}
	}

	@Override
	public void init() {
		AudioStreamManager.INSTANCE = new AudioStreamManagerClient();
	}

	@Override
	public void registerItemModel(Item item, int meta, String name) {
		if (name.contains("#")) {
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name.split("#")[0], name.split("#")[1]));
		} else {
			ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name, "inventory"));
		}
	}

	@Override
	public World getLocalWorld(int dim) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			World w = Minecraft.getMinecraft().world;
			if (w != null && w.provider.getDimension() == dim) {
				return w;
			} else {
				return null;
			}
		} else {
			return DimensionManager.getWorld(dim);
		}
	}

	@Override
	public void onServerStop() {
		AudioStreamManagerClient.INSTANCE.removeAll();
	}

	@Override
	public boolean isMainThread() {
		return Minecraft.getMinecraft().isCallingFromMinecraftThread();
	}

	@Override
	public void addScheduledMainTask(Runnable runnable) {
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}

	@Override
	public boolean isClient() {
		return true;
	}
}
