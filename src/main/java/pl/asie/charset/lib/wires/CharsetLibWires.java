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

package pl.asie.charset.lib.wires;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
	name = "lib.wires",
	description = "Wire support module",
	dependencies = {"mod:mcmultipart"},
	profile = ModuleProfile.STABLE
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
		blockWire = new BlockWire();
		itemWire = new ItemWire(blockWire);

		WireManager.REGISTRY = (ForgeRegistry<WireProvider>) new RegistryBuilder<WireProvider>()
				.setName(new ResourceLocation("charset:wire"))
				.setIDRange(1, WireManager.MAX_ID)
				.setType(WireProvider.class)
				.create();
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
		for (WireProvider provider : WireManager.REGISTRY) {
			provider.generateBoxes();
		}

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
		WireManager.REGISTRY.unfreeze();

		for (WireProvider provider : WireManager.REGISTRY) {
			rendererWire.registerSheet(event.getMap(), provider);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:wire", "redstone=false"), rendererWire);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:wire", "redstone=true"), rendererWire);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:wire", "inventory"), rendererWire);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new WireHighlightHandler());
	}
}
