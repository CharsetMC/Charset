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

package pl.asie.charset.farming;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.lib.ModCharsetLib;

@Mod(modid = ModCharsetFarming.MODID, name = ModCharsetFarming.NAME, version = ModCharsetFarming.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetFarming {
	public static final String MODID = "CharsetFarming";
	public static final String NAME = ";";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(clientSide = "pl.asie.charset.farming.ProxyClient", serverSide = "pl.asie.charset.farming.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.Instance(MODID)
	public static ModCharsetFarming instance;
	public static Logger logger;

	public static UniversalBucket clayBucket;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetFarming.MODID);

		clayBucket = new UniversalClayBucket();
		GameRegistry.register(clayBucket.setRegistryName("clayBucket"));
		ModCharsetLib.proxy.registerItemModel(clayBucket, 0, "charsetfarming:clayBucket");

		MinecraftForge.EVENT_BUS.register(proxy);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(clayBucket),
				"C C", " C ",
				'C', Items.CLAY_BALL
		));
	}
}
