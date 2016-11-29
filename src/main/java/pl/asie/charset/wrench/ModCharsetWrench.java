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

package pl.asie.charset.wrench;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pl.asie.charset.lib.ModCharsetLib;

@Mod(modid = ModCharsetWrench.MODID, name = ModCharsetWrench.NAME, version = ModCharsetWrench.VERSION,
		dependencies = ModCharsetLib.DEP_DEFAULT, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetWrench {
	public static final String MODID = "charsetwrench";
	public static final String NAME = "/";
	public static final String VERSION = "@VERSION@";

	public static ItemWrench wrench;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		wrench = new ItemWrench();
		GameRegistry.register(wrench.setRegistryName("wrench"));
		ModCharsetLib.proxy.registerItemModel(wrench, 0, "charsetwrench:wrench");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wrench),
				" i ", " si", "i  ", 's', "stickWood", 'i', "ingotIron"));
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
