/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.module.decoration.signs;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.render.sprite.TextureWhitener;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "decoration.signs",
		description = "Charset's Signature Signs!",
		profile = ModuleProfile.INDEV
)
public class CharsetDecorationSigns {
	public static BlockSign blockSign;
	public static ItemSign itemSign;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		blockSign = new BlockSign();
		itemSign = new ItemSign();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileSign.class, "sign");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemColorHandler(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler(new ItemSign.Color(), itemSign);
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockSign, "sign");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemSign, "sign");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(itemSign, 0, "charset:sign");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		event.getMap().setTextureEntry(
				TextureWhitener.INSTANCE.remap(
						new ResourceLocation("minecraft:items/sign"),
						new ResourceLocation("charset:items/sign"),
						new ResourceLocation("minecraft:items/sign")
				)
		);
	}
}
