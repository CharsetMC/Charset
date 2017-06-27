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

package pl.asie.charset.module.misc;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
		name = "misc.graphite",
		description= "Adds a black dye replacement created from charcoal"
)
public class CharsetMiscGraphite {
	private Item graphite;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		graphite = new Item().setUnlocalizedName("charset.graphite");

		ItemStack stack = new ItemStack(graphite, 1, 0);
		OreDictionary.registerOre("dye", stack);
		OreDictionary.registerOre("dyeBlack", stack);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), graphite, "graphite");
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(graphite, 0, "charset:graphite");
	}
}
