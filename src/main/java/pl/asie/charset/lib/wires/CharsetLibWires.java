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

package pl.asie.charset.lib.wires;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.ThreeState;

@CharsetModule(
	name = "lib.wires",
	description = "Wire support module",
	dependencies = {"mod:mcmultipart"}
)
public class CharsetLibWires {
	@CharsetModule.Instance
	public static CharsetLibWires instance;

	public static BlockWire blockWire;
	public static ItemWire itemWire;

	@SideOnly(Side.CLIENT)
	private RendererWire rendererWire;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		WireManager.REGISTRY.toString(); // Poke REGISTRY <- this is a hack to initialize it

		RegistryUtils.register(blockWire = new BlockWire(), itemWire = new ItemWire(blockWire), "wire");

		for (int i = 0; i < WireManager.MAX_ID * 2; i++) {
			RegistryUtils.registerModel(itemWire, i, "charset:wire");
		}
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		rendererWire = new RendererWire();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		for (WireProvider provider : WireManager.REGISTRY) {
			rendererWire.registerSheet(event.getMap(), provider);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:wire", "normal"), rendererWire);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:wire", "inventory"), rendererWire);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		RegistryUtils.register(TileWire.class, "wire");

		// Add default conversion recipes
		for (WireProvider provider : WireManager.REGISTRY) {
			if (provider.hasFreestandingWire() && provider.hasSidedWire()) {
				RecipeCharset.Builder.create(new RecipeResultWire(provider, false, 1))
						.shapeless(new RecipeObjectWire(provider, ThreeState.YES)).register();
				RecipeCharset.Builder.create(new RecipeResultWire(provider, true, 1))
						.shapeless(new RecipeObjectWire(provider, ThreeState.NO)).register();
			}
		}
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new WireHighlightHandler());
	}
}
