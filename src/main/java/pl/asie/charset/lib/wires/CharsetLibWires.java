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

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
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
		blockWire = new BlockWire();
		itemWire = new ItemWire(blockWire);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		for (int i = 0; i < WireManager.MAX_ID * 2; i++) {
			RegistryUtils.registerModel(itemWire, i, "charset:wire");
		}
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockWire, "wire");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemWire, "wire");
	}

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		event.getRegistry().register(new RecipeWireConversion(false).setRegistryName("charset:wire_conversion_to"));
		event.getRegistry().register(new RecipeWireConversion(true).setRegistryName("charset:wire_conversion_from"));
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileWire.class, "wire");
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
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new WireHighlightHandler());
	}
}
