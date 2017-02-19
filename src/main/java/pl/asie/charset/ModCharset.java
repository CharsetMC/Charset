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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.asie.charset.datafixes.CharsetUnifiedModIdFixer;
import pl.asie.charset.lib.CharsetIMC;
import pl.asie.charset.lib.annotation.AnnotationHandler;
import pl.asie.charset.lib.misc.IconCharset;
import pl.asie.charset.lib.ui.GuiHandlerCharset;
import pl.asie.charset.lib.utils.RegistryUtils;

import java.io.File;
import java.util.Set;

@Mod(modid = ModCharset.MODID, name = ModCharset.NAME, version = ModCharset.VERSION, updateJSON = ModCharset.UPDATE_URL, dependencies = ModCharset.DEP_LIB)
public class ModCharset {
	public static final String UPDATE_URL = "http://charset.asie.pl/update.json";
	public static final String MODID = "charset";
	public static final String NAME = "Charset";
	public static final String VERSION = "@VERSION@";
	public static final String DEP_LIB = "required-after:forge@[13.19.1.2188,);after:jei@[4.0.5.201,);before:betterwithmods;before:mcmultipart";

	public static final boolean INDEV = ("@version@".equals(VERSION.toLowerCase()));

	@Mod.Instance(value = ModCharset.MODID)
	public static ModCharset instance;

	public static Item charsetIconItem;
	public static ItemStack charsetIconStack;

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
		dataFixes = FMLCommonHandler.instance().getDataFixer().init(ModCharset.MODID, 1);
		dataFixes.registerFix(FixTypes.ENTITY, new CharsetUnifiedModIdFixer.Entity(oldPrefixes));

		AnnotationHandler.INSTANCE.preInit(event.getAsmData());

		charsetIconItem = new IconCharset();
		GameRegistry.register(charsetIconItem.setRegistryName("icon"));
		charsetIconStack = new ItemStack(charsetIconItem);

		RegistryUtils.registerModel(charsetIconItem, 0, "charset:icon");

		AnnotationHandler.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandlerCharset.INSTANCE);

		AnnotationHandler.INSTANCE.init();
		AnnotationHandler.INSTANCE.passEvent(event);

		if (configIds.hasChanged()) configIds.save();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		AnnotationHandler.INSTANCE.postInit();
		AnnotationHandler.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerAboutToStartEvent event) {
		AnnotationHandler.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStartingEvent event) {
		AnnotationHandler.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStartedEvent event) {
		AnnotationHandler.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStoppingEvent event) {
		AnnotationHandler.INSTANCE.passEvent(event);
	}

	@Mod.EventHandler
	public void passThrough(FMLServerStoppedEvent event) {
		AnnotationHandler.INSTANCE.passEvent(event);
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

	@Mod.EventHandler
	public void onMissingMappings(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
			if (oldPrefixes.contains(mapping.resourceLocation.getResourceDomain())) {
				ResourceLocation newName = new ResourceLocation("charset", mapping.resourceLocation.getResourcePath());
				if (mapping.type == GameRegistry.Type.BLOCK) {
					Block b = Block.getBlockFromName(newName.toString());
					if (b != null && b != Blocks.AIR) {
						mapping.remap(b);
					} else {
						mapping.warn();
					}
				} else if (mapping.type == GameRegistry.Type.ITEM) {
					Item b = Item.getByNameOrId(newName.toString());
					if (b != null) {
						mapping.remap(b);
					} else {
						mapping.warn();
					}
				}
			}
		}
	}

	public static boolean isModuleLoaded(String s) {
		return AnnotationHandler.INSTANCE.getLoadedModules().contains(s);
	}
}
