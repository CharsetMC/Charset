/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.lib;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.CharsetAPI;
import pl.asie.charset.api.audio.AudioAPI;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.lib.audio.PacketAudioData;
import pl.asie.charset.lib.audio.PacketAudioStop;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;
import pl.asie.charset.lib.audio.types.AudioDataDFPWM;
import pl.asie.charset.lib.audio.types.AudioDataGameSound;
import pl.asie.charset.lib.audio.types.AudioSinkBlock;
import pl.asie.charset.lib.block.PacketCustomBlockDust;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.command.*;
import pl.asie.charset.lib.config.CharsetLoadConfigEvent;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.lib.handlers.*;
import pl.asie.charset.lib.item.SubItemProviderCache;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterialHeuristics;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.notify.NotifyImplementation;
import pl.asie.charset.lib.notify.PacketNotification;
import pl.asie.charset.lib.notify.PacketPoint;
import pl.asie.charset.lib.recipe.IngredientColor;
import pl.asie.charset.lib.recipe.RecipeReplacement;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.resources.CharsetFakeResourcePack;
import pl.asie.charset.lib.scheduler.Scheduler;
import pl.asie.charset.lib.utils.*;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CharsetModule(
	name = "lib",
	description = "Library module",
	profile = ModuleProfile.STABLE,
	isVisible = false
)
public class CharsetLib {
	@CharsetModule.Instance
	public static CharsetLib instance;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	@CharsetModule.Configuration
	public static Configuration config;

	public static Supplier<Calendar> calendar = Suppliers.memoizeWithExpiration(new Supplier<Calendar>() {
		@Override
		public Calendar get() {
			return Calendar.getInstance();
		}
	}, 1, TimeUnit.MINUTES);

	public static PlayerDeathHandler deathHandler = new PlayerDeathHandler();

	public static boolean alwaysDropDroppablesGivenToPlayer;
	public static boolean enableDebugInfo;
	public static boolean showAllItemTypes;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		ModelFactory.clearCaches();
		ColorLookupHandler.INSTANCE.clear();
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unchecked")
	public void preInitClient(FMLPreInitializationEvent event) {
		try {
			Field field = ReflectionHelper.findField(Minecraft.class, "defaultResourcePacks", "field_110449_ao");
			((List) field.get(Minecraft.getMinecraft())).add(CharsetFakeResourcePack.INSTANCE);

			((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(CharsetFakeResourcePack.INSTANCE);

			// TODO: Can we get rid of this to save a bit of loading time?
			// (We can, but it involves loading Minecraft.<init> a bit early.
			// Hmm.)
			Minecraft.getMinecraft().refreshResources();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Mod.EventHandler
	public void loadConfig(CharsetLoadConfigEvent event) {
		alwaysDropDroppablesGivenToPlayer = ConfigUtils.getBoolean(config, "general", "alwaysDropDroppablesGivenToPlayer", false, "Setting this option to true will stop Charset from giving players items directly into the player inventory when the alternative is dropping it (for instance, taking item out of barrels).", true);
		enableDebugInfo = ConfigUtils.getBoolean(config, "expert","enableDebugInfo", ModCharset.INDEV, "Enable developer debugging information. Don't enable this unless asked/you know what you're doing.", false);

		boolean oldShowAllItemTypes = showAllItemTypes;
		showAllItemTypes = ConfigUtils.getBoolean(config, "general","showAllItemTypes", ModCharset.INDEV, "Make mods such as JEI show all combinations of a given item (within reason), as opposed to a random selection.", false);
		if (!event.isFirstTime() && oldShowAllItemTypes != showAllItemTypes) {
			SubItemProviderCache.clear();
		}

		if (event.isFirstTime()) {
			CharsetIMC.INSTANCE.loadConfig(config);
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		AudioAPI.DATA_REGISTRY = new CharsetSimpleInstantiatingRegistry<>();
		AudioAPI.SINK_REGISTRY = new CharsetSimpleInstantiatingRegistry<>();

		Capabilities.preInit();
		NotifyImplementation.init();
		ItemMaterialHeuristics.init(false);

		MinecraftForge.EVENT_BUS.register(new CharsetLibEventHandler());
		MinecraftForge.EVENT_BUS.register(Scheduler.INSTANCE);

		config.save();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		RecipeReplacement.PRIMARY.process(event.getRegistry().getValues());
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new SplashTextHandler());

		ClientCommandHandler.instance.registerCommand(CommandCharset.CLIENT);

		CommandCharset.register(new SubCommandClientCmdList("day", "Makes it day", "/time set 1200"));
		CommandCharset.register(new SubCommandClientCmdList("night", "Makes it night", "/time set 18000"));
		CommandCharset.register(new SubCommandClientCmdList("nice", "Makes it a sunny morning", "/time set 1200", "/weather clear"));
		CommandCharset.register(new SubCommandFog());
		CommandCharset.register(new SubCommandSetupTestWorld());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		packet.registerPacket(0x01, PacketAudioData.class);
		packet.registerPacket(0x02, PacketAudioStop.class);

		packet.registerPacket(0x10, PacketNotification.class);
		packet.registerPacket(0x11, PacketPoint.class);

		packet.registerPacket(0x20, PacketCustomBlockDust.class);

		packet.registerPacket(0x30, PacketRequestScroll.class);

		MinecraftForge.EVENT_BUS.register(ShiftScrollHandler.INSTANCE);

		Capabilities.init();
		DataSerializersCharset.init();
		UtilProxyCommon.proxy.init();

		if (ModCharset.INDEV || enableDebugInfo)
			MinecraftForge.EVENT_BUS.register(new DebugInfoProvider());

		CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioData.class).register(AudioDataDFPWM.class, AudioDataDFPWM::new);
		CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioData.class).register(AudioDataGameSound.class, AudioDataGameSound::new);
		CharsetAPI.INSTANCE.findSimpleInstantiatingRegistry(AudioSink.class).register(AudioSinkBlock.class, AudioSinkBlock::new);

		CommandCharset.register(new SubCommandHelp(Side.CLIENT));
		CommandCharset.register(new SubCommandHelp(Side.SERVER));

		CommandCharset.register(new SubCommandHand());
		CommandCharset.register(new SubCommandAt());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Capabilities.registerVanillaWrappers();
		IngredientColor.registerDefaults();
		ItemMaterialHeuristics.init(true);
		SubItemProviderCache.clear();

		if (deathHandler.hasPredicate()) {
			MinecraftForge.EVENT_BUS.register(deathHandler);
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(CommandCharset.SERVER);
		NotifyImplementation.instance.registerServerCommands(event);
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		AudioStreamManager.INSTANCE.removeAll();
	}
}
