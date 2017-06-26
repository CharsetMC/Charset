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

package pl.asie.charset.module.misc.scaffold;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;

@CharsetModule(
		name = "misc.scaffold",
		description = "Adds scaffolds you can build up and climb on."
)
public class CharsetMiscScaffold {
	@CharsetModule.Instance
	public static CharsetMiscScaffold instance;

	public static Block scaffoldBlock;
	public static Item scaffoldItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (!ForgeModContainer.fullBoundingBoxLadders) {
			ModCharset.logger.warn("To make Charset scaffolds work better, we recommend enabling fullBoundingBoxLadders in forge.cfg.");
		}

		scaffoldBlock = new BlockScaffold();
		scaffoldItem = new ItemScaffold(scaffoldBlock);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(scaffoldItem, 0, "charset:scaffold");
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), scaffoldBlock, "scaffold");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), scaffoldItem, "scaffold");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (scaffoldBlock != null) {
			RegistryUtils.register(TileScaffold.class, "scaffold");
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void addCustomModels(TextureStitchEvent.Pre event) {
		ModelScaffold.scaffoldModel = RenderUtils.getModel(new ResourceLocation("charset:block/scaffold"));
		for (ResourceLocation location : ModelScaffold.scaffoldModel.getTextures()) {
			event.getMap().registerSprite(location);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:scaffold", "normal"), ModelScaffold.INSTANCE);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:scaffold", "inventory"), ModelScaffold.INSTANCE);
	}
}
