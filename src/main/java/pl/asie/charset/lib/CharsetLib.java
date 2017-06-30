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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.audio.AudioAPI;
import pl.asie.charset.lib.audio.PacketAudioData;
import pl.asie.charset.lib.audio.PacketAudioStop;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;
import pl.asie.charset.lib.audio.types.AudioDataDFPWM;
import pl.asie.charset.lib.audio.types.AudioDataGameSound;
import pl.asie.charset.lib.audio.types.AudioSinkBlock;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.command.CommandCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterialHeuristics;
import pl.asie.charset.lib.misc.DebugInfoProvider;
import pl.asie.charset.lib.misc.PlayerDeathHandler;
import pl.asie.charset.lib.misc.SplashTextHandler;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.notify.NotifyImplementation;
import pl.asie.charset.lib.notify.PacketNotification;
import pl.asie.charset.lib.notify.PacketPoint;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.resources.CharsetFakeResourcePack;
import pl.asie.charset.lib.utils.CharsetSimpleInstantiatingRegistry;
import pl.asie.charset.lib.utils.DataSerializersCharset;
import pl.asie.charset.lib.utils.UtilProxyCommon;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CharsetModule(
	name = "lib",
	description = "Library module",
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
	// public static boolean showAllItemTypes;

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

			// TODO: Can we get rid of this to save a bit of loading time?c
			Minecraft.getMinecraft().refreshResources();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		AudioAPI.DATA_REGISTRY = new CharsetSimpleInstantiatingRegistry<>();
		AudioAPI.SINK_REGISTRY = new CharsetSimpleInstantiatingRegistry<>();

		alwaysDropDroppablesGivenToPlayer = config.getBoolean("alwaysDropDroppablesGivenToPlayer", "general", false, "Setting this option to true will stop Charset from giving players items directly into the player inventory when the alternative is dropping it (for instance, taking item out of barrels).");
		enableDebugInfo = config.getBoolean("enableDebugInfo", "expert", ModCharset.INDEV, "Enable developer debugging information. Don't enable this unless asked/you know what you're doing.");
		// showAllItemTypes = config.getBoolean("showAllItemTypes", "general", true, "Make mods such as JEI show all combinations of a given item (within reason), as opposed to a random selection.");

		Capabilities.preInit();
		NotifyImplementation.init();
		ItemMaterialHeuristics.init(false);
		CharsetIMC.INSTANCE.loadConfig(config);

		MinecraftForge.EVENT_BUS.register(new CharsetLibEventHandler());

		config.save();
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new SplashTextHandler());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		packet.registerPacket(0x01, PacketAudioData.class);
		packet.registerPacket(0x02, PacketAudioStop.class);

		packet.registerPacket(0x10, PacketNotification.class);
		packet.registerPacket(0x11, PacketPoint.class);

		Capabilities.init();
		DataSerializersCharset.init();
		UtilProxyCommon.proxy.init();

		if (ModCharset.INDEV || enableDebugInfo)
			MinecraftForge.EVENT_BUS.register(new DebugInfoProvider());

		AudioAPI.DATA_REGISTRY.register(AudioDataDFPWM.class, AudioDataDFPWM::new);
		AudioAPI.DATA_REGISTRY.register(AudioDataGameSound.class, AudioDataGameSound::new);
		AudioAPI.SINK_REGISTRY.register(AudioSinkBlock.class, AudioSinkBlock::new);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Capabilities.registerVanillaWrappers();
		ItemMaterialHeuristics.init(true);

		if (deathHandler.hasPredicate()) {
			MinecraftForge.EVENT_BUS.register(deathHandler);
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandCharset());
		NotifyImplementation.instance.registerServerCommands(event);
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		AudioStreamManager.INSTANCE.removeAll();
	}
}
