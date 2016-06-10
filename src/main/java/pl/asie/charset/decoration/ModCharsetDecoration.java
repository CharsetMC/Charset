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

package pl.asie.charset.decoration;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.decoration.poster.EntityPoster;
import pl.asie.charset.decoration.poster.ItemPoster;
import pl.asie.charset.lib.ModCharsetLib;

@Mod(modid = ModCharsetDecoration.MODID, name = ModCharsetDecoration.NAME, version = ModCharsetDecoration.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetDecoration {
	public static final String MODID = "CharsetDecoration";
	public static final String NAME = "^";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.decoration.ProxyClient", serverSide = "pl.asie.charset.decoration.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.Instance(MODID)
	public static ModCharsetDecoration instance;
	public static Logger logger;

	public static Item posterItem;

	private Configuration config;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetDecoration.MODID);
		config = new Configuration(ModCharsetLib.instance.getConfigFile("decoration.cfg"));

		posterItem = new ItemPoster();
		GameRegistry.register(posterItem.setRegistryName("poster"));
		ModCharsetLib.proxy.registerItemModel(posterItem, 0, "charsetdecoration:poster");

		proxy.preInit();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		EntityRegistry.registerModEntity(EntityPoster.class, "charsetdecoration:poster", 1, this, 64, 3, true);

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(posterItem),
				"0",
				"-",
				"0",
				'-', Items.PAPER, '0', "slimeball"
		));
	}
}
