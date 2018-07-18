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

package pl.asie.charset.module.crafting.pocket;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.inventory.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "crafting.pocket",
		description = "Adds a Pocket Crafting Table",
		profile = ModuleProfile.STABLE
)
public class CharsetCraftingPocket {
	@CharsetModule.Instance
	public static CharsetCraftingPocket instance;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	public static ItemPocketTable pocketTable;
	public static String pocketActions = "xcbf";

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		pocketTable = new ItemPocketTable();
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), pocketTable, "pocketTable");
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(pocketTable, 0, "charset:pocket_crafting_table");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		packet.registerPacket(0x01, PacketPTAction.class);
		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.POCKET_TABLE, Side.SERVER, (r) -> new ContainerPocketTable(r.player));
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		GuiHandlerCharset.INSTANCE.register(GuiHandlerCharset.POCKET_TABLE, Side.CLIENT, (r) -> new GuiPocketTable(r.getContainer(ContainerPocketTable.class)));
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
