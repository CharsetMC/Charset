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

package pl.asie.charset.lib;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import pl.asie.charset.api.audio.AudioAPI;
import pl.asie.charset.lib.audio.AudioDataDFPWM;
import pl.asie.charset.lib.audio.AudioDataSound;
import pl.asie.charset.lib.audio.AudioSinkBlock;
import pl.asie.charset.lib.audio.PacketAudioData;
import pl.asie.charset.lib.audio.PacketAudioStop;
import pl.asie.charset.lib.misc.IconCharset;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.notify.NotifyImplementation;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.lib.recipe.RecipeDyeableItem;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.DataSerializersCharset;
import pl.asie.charset.lib.wires.ItemWire;
import pl.asie.charset.lib.wires.RecipeObjectWire;
import pl.asie.charset.lib.wires.RecipeResultWire;
import pl.asie.charset.lib.wires.WireFactory;
import pl.asie.charset.lib.wires.WireManager;

@Mod(modid = ModCharsetLib.MODID, name = ModCharsetLib.NAME, version = ModCharsetLib.VERSION, updateJSON = ModCharsetLib.UPDATE_URL, dependencies = "after:mcmultipart;after:JEI@[3.13.3.373,)", acceptableRemoteVersions = ModCharsetLib.ACCEPTABLE_REMOTE_VERSIONS)
public class ModCharsetLib {
	public static final boolean INDEV = false;

	public static final String UPDATE_URL = "http://charset.asie.pl/update.json";
	public static final String MODID = "CharsetLib";
	public static final String NAME = "‽";
	public static final String VERSION = "@VERSION@";
	public static final String DEP_MCMP = "required-after:Forge@[11.15.0.1715,);required-after:CharsetLib@" + VERSION + ";required-after:mcmultipart@[1.3.0,)";
	public static final String DEP_NO_MCMP = "required-after:Forge@[11.15.0.1715,);required-after:CharsetLib@" + VERSION + ";after:mcmultipart";
	public static final String ACCEPTABLE_REMOTE_VERSIONS = "[0.3.5,0.3.6]";

	public static final String MODULE_PIPES = "pipes";
	public static final String MODULE_TWEAKS = "tweaks";
	public static final String MODULE_WIRES = "wires";
	public static final String MODULE_GATES = "gates";
	public static final String MODULE_AUDIO = "audio";
	public static final String MODULE_STORAGE = "storage";
	public static final String MODULE_DECORATION = "decoration";
	public static final String MODULE_DRAMA = "drama";
	public static final String MODULE_FARMING = "farming";
	public static final String MODULE_WRENCH = "wrench";
	
	public static final String[] MODULES = {
		MODULE_PIPES, MODULE_TWEAKS, MODULE_WIRES, MODULE_GATES, MODULE_AUDIO, MODULE_STORAGE, MODULE_DECORATION, MODULE_DRAMA, MODULE_WRENCH, MODULE_FARMING
	};
	private static final ImmutableSet<String> DISABLED_MODULES_BY_DEFAULT = ImmutableSet.of(MODULE_DRAMA, MODULE_FARMING);
	private static final HashMap<String, Boolean> moduleStatus = new HashMap();
	
	public static Supplier<Calendar> calendar = Suppliers.memoizeWithExpiration(new Supplier<Calendar>() {
		@Override
		public Calendar get() {
			return Calendar.getInstance();
		}
	}, 1, TimeUnit.MINUTES);

	@Mod.Instance(value = ModCharsetLib.MODID)
	public static ModCharsetLib instance;

	public static PacketRegistry packet;

	@SidedProxy(clientSide = "pl.asie.charset.lib.ProxyClient", serverSide = "pl.asie.charset.lib.ProxyCommon")
	public static ProxyCommon proxy;

	public static PlayerDeathHandler deathHandler = new PlayerDeathHandler();

	public static IconCharset charsetIconItem;

	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("charset") {
		@Override
		public Item getTabIconItem() {
			return charsetIconItem;
		}
	};
	public static Logger logger;

	private File configurationDirectory;

	public File getModuleConfigFile() {
		return getConfigFile("_modules.cfg");
	}
	
	public File getConfigFile(String filename) {
		return new File(configurationDirectory, filename);
	}
	
	public static boolean moduleEnabled(String module) {
		return moduleStatus.get(module);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(MODID);

		configurationDirectory = new File(event.getModConfigurationDirectory(), "charset");
		if (!configurationDirectory.exists()) {
			configurationDirectory.mkdir();
		}

		charsetIconItem = new IconCharset();
		GameRegistry.register(charsetIconItem.setRegistryName("icon"));

		WireManager.ITEM = new ItemWire();
		GameRegistry.register(WireManager.ITEM.setRegistryName("wire"));

		for (int i = 0; i < 512; i++) { // TODO
			ModCharsetLib.proxy.registerItemModel(WireManager.ITEM, i, "charsetlib:wire");
		}

		proxy.registerItemModel(charsetIconItem, 0, "charsetlib:icon");

		Capabilities.init();
		NotifyImplementation.init();
		
		Configuration config = new Configuration(getModuleConfigFile());
		config.load();
		for(String s : MODULES)
			moduleStatus.put(s, config.getBoolean(s, config.CATEGORY_GENERAL, !DISABLED_MODULES_BY_DEFAULT.contains(s), ""));
		if(config.hasChanged())
			config.save();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();

		packet = new PacketRegistry(ModCharsetLib.MODID);
		packet.registerPacket(0x01, PacketAudioData.class);
		packet.registerPacket(0x02, PacketAudioStop.class);

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(proxy);

		ColorUtils.init();
		DataSerializersCharset.init();

		GameRegistry.addRecipe(new RecipeDyeableItem());
		RecipeSorter.register("charsetDyeable", RecipeDyeableItem.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter.register("charset", RecipeCharset.class, RecipeSorter.Category.UNKNOWN, "before:minecraft:shaped");

		for (WireFactory factory : WireManager.REGISTRY.getValues()) {
			GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(factory, false, 1))
					.shapeless(new RecipeObjectWire(factory, true)).build());
			GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(factory, true, 1))
					.shapeless(new RecipeObjectWire(factory, false)).build());
		}

		AudioAPI.DATA_REGISTRY.register(AudioDataDFPWM.class);
		AudioAPI.DATA_REGISTRY.register(AudioDataSound.class);
		AudioAPI.SINK_REGISTRY.register(AudioSinkBlock.class);
	}

	@Mod.EventHandler
	public void postInit(FMLInitializationEvent event) {
		if (deathHandler.hasPredicate()) {
			MinecraftForge.EVENT_BUS.register(deathHandler);
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		NotifyImplementation.instance.registerServerCommands(event);
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		proxy.onServerStop();
	}
}
