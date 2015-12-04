package pl.asie.charset.tweaks;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import pl.asie.charset.lib.ModCharsetLib;

@Mod(modid = ModCharsetTweaks.MODID, name = ModCharsetTweaks.NAME, version = ModCharsetTweaks.VERSION,
	dependencies = "required-after:CharsetLib@" + ModCharsetTweaks.VERSION, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetTweaks {
	public static final String MODID = "CharsetTweaks";
	public static final String NAME = "*";
	public static final String VERSION = "@VERSION@";

	private Configuration configuration;

	private final Set<Tweak> tweakSet = new HashSet<Tweak>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		tweakSet.add(new TweakAutoReplace());
		tweakSet.add(new TweakGraphite());

		configuration = new Configuration(ModCharsetLib.instance.getConfigFile("tweaks.cfg"));

		for (Tweak t : tweakSet) {
			t.onConfigChanged(configuration, true);
			if (t.isEnabled()) {
				t.preInit();
			}
		}

		configuration.save();
    }

	@EventHandler
	public void init(FMLInitializationEvent event) {
		for (Tweak t : tweakSet) {
			if (t.isEnabled()) {
				t.init();
			}
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
		if (ModCharsetTweaks.MODID.equals(event.modID)) {
			for (Tweak t : tweakSet) {
				t.onConfigChanged(configuration, false);
			}
		}
	}
}
