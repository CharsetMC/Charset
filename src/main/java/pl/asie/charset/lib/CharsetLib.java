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
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import pl.asie.charset.ModCharset;
import pl.asie.charset.api.audio.AudioAPI;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.audio.PacketAudioData;
import pl.asie.charset.lib.audio.PacketAudioStop;
import pl.asie.charset.lib.audio.manager.AudioStreamManager;
import pl.asie.charset.lib.audio.types.AudioDataDFPWM;
import pl.asie.charset.lib.audio.types.AudioDataGameSound;
import pl.asie.charset.lib.audio.types.AudioSinkBlock;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.material.ColorLookupHandler;
import pl.asie.charset.lib.material.ItemMaterialHeuristics;
import pl.asie.charset.lib.misc.DebugInfoProvider;
import pl.asie.charset.lib.misc.PlayerDeathHandler;
import pl.asie.charset.lib.misc.SplashTextHandler;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.notify.NotifyImplementation;
import pl.asie.charset.lib.notify.PacketNotification;
import pl.asie.charset.lib.notify.PacketPoint;
import pl.asie.charset.lib.recipe.RecipeCharset;
import pl.asie.charset.lib.recipe.RecipeDyeableItem;
import pl.asie.charset.lib.render.model.ModelFactory;
import pl.asie.charset.lib.utils.ColorUtils;
import pl.asie.charset.lib.utils.DataSerializersCharset;

import java.util.Calendar;
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
	public void onPostBake(ModelBakeEvent event) {
		//	event.getModelRegistry().putObject(new ModelResourceLocation("charsetlib:wire", "multipart"), rendererWire);
		//	event.getModelRegistry().putObject(new ModelResourceLocation("charsetlib:wire", "inventory"), rendererWire);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event) {
		ModelFactory.clearCaches();
		ColorLookupHandler.INSTANCE.clear();

		//	for (WireFactory factory : WireManager.REGISTRY.getValues()) {
		//		rendererWire.registerSheet(event.getMap(), factory);
		//	}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		alwaysDropDroppablesGivenToPlayer = config.getBoolean("alwaysDropDroppablesGivenToPlayer", "general", false, "Setting this option to true will stop Charset from giving players items directly into the player inventory when the alternative is dropping it (for instance, taking items out of barrels).");
		enableDebugInfo = config.getBoolean("enableDebugInfo", "expert", false, "Enable developer debugging information. Don't enable this unless asked/you know what you're doing.");
		// showAllItemTypes = config.getBoolean("showAllItemTypes", "general", true, "Make mods such as JEI show all combinations of a given item (within reason), as opposed to a random selection.");

		// TODO 1.11
//		WireManager.ITEM = new ItemWire();
//		GameRegistry.register(WireManager.ITEM.setRegistryName("wire"));

//		for (int i = 0; i < 512; i++) { // TODO
//			ModCharset.proxy.registerItemModel(WireManager.ITEM, i, "charsetlib:wire");
//		}

		Capabilities.preInit();
		NotifyImplementation.init();

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

		MinecraftForge.EVENT_BUS.register(this);

		Capabilities.init();
		ColorUtils.init();
		DataSerializersCharset.init();

		if (ModCharset.INDEV || enableDebugInfo)
			MinecraftForge.EVENT_BUS.register(new DebugInfoProvider());

		GameRegistry.addRecipe(new RecipeDyeableItem());
		RecipeSorter.register("charsetDyeable", RecipeDyeableItem.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
		RecipeSorter.register("charset", RecipeCharset.class, RecipeSorter.Category.UNKNOWN, "before:minecraft:shaped");

//		for (WireFactory factory : WireManager.REGISTRY.getValues()) {
//			GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(factory, false, 1))
//					.shapeless(new RecipeObjectWire(factory, true)).build());
//			GameRegistry.addRecipe(RecipeCharset.Builder.create(new RecipeResultWire(factory, true, 1))
//					.shapeless(new RecipeObjectWire(factory, false)).build());
//		}

		AudioAPI.DATA_REGISTRY.register(AudioDataDFPWM.class);
		AudioAPI.DATA_REGISTRY.register(AudioDataGameSound.class);
		AudioAPI.SINK_REGISTRY.register(AudioSinkBlock.class);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Capabilities.registerVanillaWrappers();

		if (deathHandler.hasPredicate()) {
			MinecraftForge.EVENT_BUS.register(deathHandler);
		}

		ItemMaterialHeuristics.init();
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		NotifyImplementation.instance.registerServerCommands(event);
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppedEvent event) {
		AudioStreamManager.INSTANCE.removeAll();
	}
}
