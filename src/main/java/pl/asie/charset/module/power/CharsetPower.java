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

package pl.asie.charset.module.power;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.modcompat.crafttweaker.Registry;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.power.mechanical.*;
import pl.asie.charset.module.power.mechanical.render.TileAxleRenderer;

@CharsetModule(
		name = "power",
		description = "Mechanical power! And steam power!",
		profile = ModuleProfile.INDEV
)
public class CharsetPower {
	public static BlockAxle blockAxle;
	public static BlockCreativeGenerator blockCreativeGenerator;
	public static BlockGearbox blockGearbox;
	public static BlockSocket blockSocket;
	public static ItemBlock itemAxle, itemCreativeGenerator, itemGearbox, itemSocket;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		blockAxle = new BlockAxle();
		blockCreativeGenerator = new BlockCreativeGenerator();
		blockGearbox = new BlockGearbox();
		blockSocket = new BlockSocket();
		itemAxle = new ItemBlockAxle(blockAxle);
		itemCreativeGenerator = new ItemBlockBase(blockCreativeGenerator);
		itemGearbox = new ItemBlockBase(blockGearbox);
		itemSocket = new ItemBlockBase(blockSocket);
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		RegistryUtils.register(TileAxle.class, "axle");
		RegistryUtils.register(TileCreativeGenerator.class, "creative_generator");
		RegistryUtils.register(TileGearbox.class, "gearbox");
		RegistryUtils.register(TileSocket.class, "socket");
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPreInitClient(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(TileAxleRenderer.INSTANCE);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onInitClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileAxle.class, TileAxleRenderer.INSTANCE);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelRegistry(ModelRegistryEvent event) {
		RegistryUtils.registerModel(itemAxle, 0, "charset:axle");
		RegistryUtils.registerModel(itemCreativeGenerator, 0, "charset:gen_creative");
		RegistryUtils.registerModel(itemGearbox, 0, "charset:gearbox");
		RegistryUtils.registerModel(itemSocket, 0, "charset:socket");
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockAxle, "axle");
		RegistryUtils.register(event.getRegistry(), blockCreativeGenerator, "creative_generator");
		RegistryUtils.register(event.getRegistry(), blockGearbox, "gearbox");
		RegistryUtils.register(event.getRegistry(), blockSocket, "socket");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemAxle, "axle");
		RegistryUtils.register(event.getRegistry(), itemCreativeGenerator, "creative_generator");
		RegistryUtils.register(event.getRegistry(), itemGearbox, "gearbox");
		RegistryUtils.register(event.getRegistry(), itemSocket, "socket");
	}
}
