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

package pl.asie.charset;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
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
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.api.CharsetAPI;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.CharsetImplAPI;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.loader.ModuleLoader;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.misc.IconCharset;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.misc.FixCharsetUnifyModId;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mod(modid = ModCharset.MODID, name = ModCharset.NAME, version = ModCharset.VERSION, updateJSON = ModCharset.UPDATE_URL, dependencies = ModCharset.DEP_LIB, guiFactory = "pl.asie.charset.lib.config.ConfigGuiFactory")
public class ModCharset {
	public static final String UPDATE_URL = "http://charset.asie.pl/update.json";
	public static final String MODID = "charset";
	public static final String NAME = "Charset";
	public static final String VERSION = "@VERSION@";
	public static final String DEP_LIB = "after:forge@[14.23.1.2571,);before:jei@[4.7.8,);before:betterwithmods;before:mcmultipart";

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
	public static Configuration configModules, configIds, configGeneral;

	public static Map<String, String> defaultOptions = new HashMap<>();
	private static File configurationDirectory;
	public static ModFixs dataFixes;

	public static File getConfigDir() {
		return configurationDirectory;
	}

	public static File getConfigFile(String filename) {
		return new File(configurationDirectory, filename);
	}

	public static File getModuleConfigFile(String id) {
		return new File(new File(configurationDirectory, "module"), id + ".cfg");
	}

	public ModCharset() {
		CharsetAPI.INSTANCE = new CharsetImplAPI();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		try {
			InputStream stream = getClass().getResourceAsStream("assets/charset/default.options");
			if (stream != null) {
				for (String s : IOUtils.toString(stream, Charsets.UTF_8).split("\n")) {
					String[] parts = s.split("=", 2);
					parts[0] = parts[0].trim();
					parts[1] = parts[1].trim();
					defaultOptions.put(parts[0].toLowerCase(), parts[1].toLowerCase());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		configurationDirectory = new File(event.getModConfigurationDirectory(), "charset");
		if (!configurationDirectory.exists()) {
			configurationDirectory.mkdir();
		}

		configModules = new Configuration(getConfigFile("modules.cfg"));
		configIds = new Configuration(getConfigFile("ids.cfg"));
		configGeneral = new Configuration(getConfigFile("charset.cfg"));
		RegistryUtils.loadConfigIds(configIds);

		logger = LogManager.getLogger();

		ModuleLoader.INSTANCE.preInit(event.getAsmData());

		dataFixes = FMLCommonHandler.instance().getDataFixer().init(ModCharset.MODID, 5);
		dataFixes.registerFix(FixTypes.ENTITY, new FixCharsetUnifyModId.Entity(oldPrefixes));

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
		RegistryUtils.register(event.getRegistry(), charsetIconItem, "icon");
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
		CharsetIMC.INSTANCE.freezeRegistries();
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStoppingEvent event) {
		CharsetIMC.INSTANCE.unfreezeRegistries();
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

	private static final Set<String> oldPrefixes = ImmutableSet.of(
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
			if (mapping.key.toString().equals("charset:wire")) {
				mapping.ignore();
				continue;
			}

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

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (ModCharset.MODID.equals(event.getModID())) {
			ModuleLoader.INSTANCE.passEvent(new CharsetLoadConfigEvent(false));
		}
	}

	public static boolean isModuleLoaded(String s) {
		return ModuleLoader.INSTANCE.getLoadedModules().contains(s);
	}
}
