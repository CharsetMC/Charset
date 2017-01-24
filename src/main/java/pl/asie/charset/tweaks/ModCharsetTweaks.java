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

package pl.asie.charset.tweaks;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.tweaks.carry.PacketCarryGrab;
import pl.asie.charset.tweaks.carry.PacketCarrySync;
import pl.asie.charset.tweaks.carry.TweakCarry;
import pl.asie.charset.tweaks.minecart.PacketMinecartRequest;
import pl.asie.charset.tweaks.minecart.PacketMinecartUpdate;
import pl.asie.charset.tweaks.minecart.TweakDyeableMinecarts;
import pl.asie.charset.tweaks.neptune.TweakMobEqualizer;
import pl.asie.charset.tweaks.neptune.TweakZorro;
import pl.asie.charset.tweaks.shard.TweakGlassShards;
import pl.asie.charset.tweaks.tnt.TweakImprovedTNT;

@Mod(modid = ModCharsetTweaks.MODID, name = ModCharsetTweaks.NAME, version = ModCharsetTweaks.VERSION,
		dependencies = ModCharsetLib.DEP_DEFAULT, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetTweaks {
	public static final String MODID = "charsettweaks";
	public static final String NAME = "*";
	public static final String VERSION = "@VERSION@";

	public static PacketRegistry packet;

	@Mod.Instance(MODID)
	public static ModCharsetTweaks instance;

	@SidedProxy(clientSide = "pl.asie.charset.tweaks.ProxyClient", serverSide = "pl.asie.charset.tweaks.ProxyCommon")
	public static pl.asie.charset.tweaks.ProxyCommon proxy;

	private Configuration configuration;

	private final Set<Tweak> tweakSet = new HashSet<Tweak>();
	private boolean canAddTweaks = true;

	public void addTweak(Tweak tweak) {
		if (canAddTweaks) {
			tweakSet.add(tweak);
		} else {
			ModCharsetLib.logger.error("Tried to add tweak too late, ignoring: " + tweak.getClass().getName());
		}
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// addTweak(new TweakAutoReplace());
		addTweak(new TweakCarry());
		addTweak(new TweakDisableEndermanGriefing());
		addTweak(new TweakDisableVanillaTools());
		addTweak(new TweakDoubleDoors());
		addTweak(new TweakDyeableMinecarts());
		addTweak(new TweakGlassShards());
		addTweak(new TweakGraphite());
		addTweak(new TweakImprovedTNT());
		addTweak(new TweakMobControl());
		addTweak(new TweakMobEqualizer());
		addTweak(new TweakNoSprinting());
		addTweak(new TweakZorro());

		configuration = new Configuration(ModCharsetLib.instance.getConfigFile("tweaks.cfg"));
		canAddTweaks = false;

		for (Tweak t : tweakSet) {
			t.onConfigChanged(configuration, true);
			if (t.isEnabled()) {
				if (!t.preInit()) {
					ModCharsetLib.logger.error("Tweak " + t.getClass().getSimpleName() + " failed to load! Please disable it in the config.");
					tweakSet.remove(t);
				}
			}
		}

		configuration.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		packet = new PacketRegistry(ModCharsetTweaks.MODID);
		packet.registerPacket(0x01, PacketMinecartUpdate.class);
		packet.registerPacket(0x02, PacketMinecartRequest.class);

		packet.registerPacket(0x11, PacketCarryGrab.class);
		packet.registerPacket(0x12, PacketCarrySync.class);

		for (Tweak t : tweakSet) {
			if (t.isEnabled()) {
				if (!t.init()) {
					ModCharsetLib.logger.error("Tweak " + t.getClass().getSimpleName() + " failed to load! Please disable it in the config.");
					tweakSet.remove(t);
				}
			}
		}

		if (configuration.hasChanged()) {
			configuration.save();
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		for (Tweak t : tweakSet) {
			if (t.isEnabled()) {
				t.enable();
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (ModCharsetTweaks.MODID.equals(event.getModID())) {
			for (Tweak t : tweakSet) {
				t.onConfigChanged(configuration, false);
			}
		}
	}
}
