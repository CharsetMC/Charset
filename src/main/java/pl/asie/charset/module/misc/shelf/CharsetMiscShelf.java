/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.module.misc.shelf;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.Properties;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.render.ShadingTintColorHandler;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;

@CharsetModule(
		name = "misc.shelf",
		description = "Reworks impl shelf and adds fancy bookshelves.",
		profile = ModuleProfile.VERY_UNSTABLE
)
public class CharsetMiscShelf {
	@CharsetModule.Instance
	public static CharsetMiscShelf instance;

	public static BlockShelf shelfBlock;
	public static ItemShelf shelfItem;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		shelfBlock = new BlockShelf();
		shelfItem = new ItemShelf(shelfBlock);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(shelfItem, 0, "charset:shelf");
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), shelfBlock, "shelf");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), shelfItem, "shelf");
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		for (Boolean backValue : BlockShelf.BACK.getAllowedValues()) {
			for (EnumFacing facingValue : Properties.FACING4.getAllowedValues()) {
				event.getModelRegistry().putObject(new ModelResourceLocation("charset:shelf", "back=" + BlockShelf.BACK.getName(backValue) + ",facing=" + Properties.FACING4.getName(facingValue)), ModelShelf.INSTANCE);
			}
		}

		event.getModelRegistry().putObject(new ModelResourceLocation("charset:shelf", "inventory"), ModelShelf.INSTANCE);

		ModelShelf.shelfModel = RenderUtils.getModel(new ResourceLocation("charset:block/shelf"));
	}
}
