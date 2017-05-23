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

package pl.asie.charset.module.misc.shelf;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.render.ShadingTintColorHandler;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;

@CharsetModule(
		name = "misc.shelf",
		description = "Reworks vanilla shelf and adds fancy bookshelves.",
		isDevOnly = true
)
public class CharsetMiscShelf {
	@CharsetModule.Instance
	public static CharsetMiscShelf instance;

	public static Block shelfBlock;
	public static Item shelfItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		shelfBlock = new BlockShelf();
		shelfItem = new ItemShelf(shelfBlock);
		RegistryUtils.register(shelfBlock, shelfItem, "shelf");
		RegistryUtils.registerModel(shelfBlock, 0, "charset:shelf");

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileShelf.class, "shelf");
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileShelf.class, new TileShelfRenderer());
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(ShadingTintColorHandler.INSTANCE, shelfBlock);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ShadingTintColorHandler.INSTANCE, shelfItem);
	}

	private void registerShelfRecipe(ItemMaterial plankMaterial) {
		ItemStack plank = plankMaterial.getStack();
		ItemStack shelf = BlockShelf.createStack(plankMaterial, 4);

		BlockShelf.PLANKS.add(plankMaterial);

		/* GameRegistry.addRecipe(new ShapedOreRecipe(shelf,
				"ppp",
				" s ",
				"s s",
				's', "stickWood", 'p', plank
		)); */
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (shelfBlock != null) {
			for (ItemMaterial plankMaterial : ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("plank", "block")) {
				registerShelfRecipe(plankMaterial);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		for (Boolean backValue : BlockShelf.BACK.getAllowedValues()) {
			for (EnumFacing facingValue : Properties.FACING4.getAllowedValues()) {
				event.getModelRegistry().putObject(new ModelResourceLocation("charset:shelf", "back=" + BlockShelf.BACK.getName(backValue) + ",facing=" + Properties.FACING4.getName(facingValue)), ModelShelf.INSTANCE);
			}
		}

		event.getModelRegistry().putObject(new ModelResourceLocation("charset:shelf", "inventory"), ModelShelf.INSTANCE);

		ModelShelf.shelfModel = (IRetexturableModel) RenderUtils.getModel(new ResourceLocation("charset:block/shelf"));
	}
}
