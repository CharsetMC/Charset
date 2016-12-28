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

package pl.asie.charset.crafting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.crafting.pocket.ItemPocketTable;
import pl.asie.charset.crafting.pocket.PacketPTAction;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;

@Mod(modid = ModCharsetCrafting.MODID, name = ModCharsetCrafting.NAME, version = ModCharsetCrafting.VERSION,
		dependencies = ModCharsetLib.DEP_DEFAULT, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetCrafting {
	public static final String MODID = "charsetcrafting";
	public static final String NAME = "#";
	public static final String VERSION = "@VERSION@";

	@Mod.Instance(MODID)
	public static ModCharsetCrafting instance;

	public static ItemPocketTable pocketTable;
	public static String pocketActions = "xcbf";

	public static PacketRegistry packet;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		pocketTable = new ItemPocketTable();
		GameRegistry.register(pocketTable.setRegistryName("pocketTable"));
		ModCharsetLib.proxy.registerItemModel(pocketTable, 0, "charsetcrafting:pocket_crafting_table");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(pocketTable),
				" c", "s ", 's', "stickWood", 'c', "workbench"));

		packet = new PacketRegistry(ModCharsetCrafting.MODID);
		packet.registerPacket(0x01, PacketPTAction.class);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerCrafting());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
