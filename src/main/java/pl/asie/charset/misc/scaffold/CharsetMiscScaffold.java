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

package pl.asie.charset.misc.scaffold;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
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
import pl.asie.charset.lib.annotation.CharsetModule;
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
		RegistryUtils.register(scaffoldBlock, scaffoldItem, "scaffold");
		RegistryUtils.registerModel(scaffoldBlock, 0, "charset:scaffold");

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (scaffoldBlock != null) {
			GameRegistry.registerTileEntityWithAlternatives(TileScaffold.class, "charset:scaffold", "charsetdecoration:scaffold");
		}
	}

	private void registerScaffoldRecipe(ItemMaterial plankMaterial) {
		ItemStack plank = plankMaterial.getStack();
		ItemStack scaffold = BlockScaffold.createStack(plankMaterial, 4);

		BlockScaffold.PLANKS.add(plankMaterial);

		GameRegistry.addRecipe(new ShapedOreRecipe(scaffold,
				"ppp",
				" s ",
				"s s",
				's', "stickWood", 'p', plank
		));
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (scaffoldBlock != null) {
			for (ItemMaterial plankMaterial : ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("plank", "block")) {
				registerScaffoldRecipe(plankMaterial);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:scaffold", "normal"), ModelScaffold.INSTANCE);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:scaffold", "inventory"), ModelScaffold.INSTANCE);

		ModelScaffold.scaffoldModel = (IRetexturableModel) RenderUtils.getModel(new ResourceLocation("charset:block/scaffold"));
	}
}
