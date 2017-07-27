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

package pl.asie.charset;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.loader.ModuleLoader;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.misc.IconCharset;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.upgrade.CharsetLockKeyTagChange;
import pl.asie.charset.upgrade.CharsetUnifiedModIdFixer;

import java.io.File;
import java.util.Set;

@Mod(modid = ModCharset.MODID, name = ModCharset.NAME, version = ModCharset.VERSION, updateJSON = ModCharset.UPDATE_URL, dependencies = ModCharset.DEP_LIB)
public class ModCharset {
	public static final String UPDATE_URL = "http://charset.asie.pl/update.json";
	public static final String MODID = "charset";
	public static final String NAME = "Charset";
	public static final String VERSION = "@VERSION@";
	public static final String DEP_LIB = "before:jei@[4.3.0,);before:betterwithmods;before:mcmultipart";

	public static final boolean INDEV = ("@version@".equals(VERSION.toLowerCase()));

	@Mod.Instance(value = ModCharset.MODID)
	public static ModCharset instance;

	private static Item charsetIconItem;
	private static ItemStack charsetIconStack;
	public static ModuleProfile profile;

	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("charset") {
		@Override
		public ItemStack getTabIconItem() {
			return charsetIconStack;
		}
	};

	public static Logger logger;
	public static Configuration configModules, configIds;

	private static File configurationDirectory;
	private static ModFixs dataFixes;

	public static File getConfigFile(String filename) {
		return new File(configurationDirectory, filename);
	}

	public static File getModuleConfigFile(String id) {
		return new File(new File(configurationDirectory, "module"), id + ".cfg");
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		configurationDirectory = new File(event.getModConfigurationDirectory(), "charset");
		if (!configurationDirectory.exists()) {
			configurationDirectory.mkdir();
		}
		configModules = new Configuration(getConfigFile("modules.cfg"));
		configIds = new Configuration(getConfigFile("ids.cfg"));
		RegistryUtils.loadConfigIds(configIds);

		logger = LogManager.getLogger();

		ModuleLoader.INSTANCE.preInit(event.getAsmData());

		dataFixes = FMLCommonHandler.instance().getDataFixer().init(ModCharset.MODID, 2);
		dataFixes.registerFix(FixTypes.ENTITY, new CharsetUnifiedModIdFixer.Entity(oldPrefixes));
		dataFixes.registerFix(FixTypes.ITEM_INSTANCE, new CharsetLockKeyTagChange());

		charsetIconItem = new IconCharset();
		charsetIconStack = new ItemStack(charsetIconItem);

		ModuleLoader.INSTANCE.passEvent(event);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(charsetIconItem, 0, "charset:icon");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(charsetIconItem.setRegistryName("icon"));
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandlerCharset.INSTANCE);

		ModuleLoader.INSTANCE.init();
		ModuleLoader.INSTANCE.passEvent(event);

		if (configIds.hasChanged()) configIds.save();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ModuleLoader.INSTANCE.postInit();
		ModuleLoader.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerAboutToStartEvent event) {
		ModuleLoader.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStartingEvent event) {
		ModuleLoader.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStartedEvent event) {
		ModuleLoader.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStoppingEvent event) {
		ModuleLoader.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStoppedEvent event) {
		ModuleLoader.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void onIMC(FMLInterModComms.IMCEvent event) {
		for (FMLInterModComms.IMCMessage message : event.getMessages()) {
			CharsetIMC.INSTANCE.receiveMessage(message);
		}
	}

	private static final Set<String> oldPrefixes = Sets.newHashSet(
			"charsetlib", "charsetpipes", "charsetstorage",
			"charsetdecoration", "charsetdrama", "charsetcarts",
			"charsettweaks", "charsetaudio", "charsetcrafting",
			"charsetwrench"
	);

	@SubscribeEvent
	public void onMissingMappingsBlock(RegistryEvent.MissingMappings<Block> event) {
		for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {
			if (oldPrefixes.contains(mapping.key.getResourceDomain())) {
				ResourceLocation newName = new ResourceLocation("charset", mapping.key.getResourcePath());
				Block b = Block.getBlockFromName(newName.toString());
				if (b != null && b != Blocks.AIR) {
					mapping.remap(b);
				} else {
					mapping.warn();
				}
			}
		}
	}

	@SubscribeEvent
	public void onMissingMappingsItem(RegistryEvent.MissingMappings<Item> event) {
		for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
			if (oldPrefixes.contains(mapping.key.getResourceDomain())) {
				ResourceLocation newName = new ResourceLocation("charset", mapping.key.getResourcePath());
				Item i = Item.getByNameOrId(newName.toString());
				if (i != null && i != Items.AIR) {
					mapping.remap(i);
				} else {
					mapping.warn();
				}
			}
		}
	}

	public static boolean isModuleLoaded(String s) {
		return ModuleLoader.INSTANCE.getLoadedModules().contains(s);
	}
}
