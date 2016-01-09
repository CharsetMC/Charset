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
import pl.asie.charset.tweaks.minecart.PacketMinecartRequest;
import pl.asie.charset.tweaks.minecart.PacketMinecartUpdate;
import pl.asie.charset.tweaks.minecart.TweakDyeableMinecarts;
import pl.asie.charset.tweaks.noteblock.TweakImprovedNoteblock;

@Mod(modid = ModCharsetTweaks.MODID, name = ModCharsetTweaks.NAME, version = ModCharsetTweaks.VERSION,
	dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetTweaks {
	public static final String MODID = "CharsetTweaks";
	public static final String NAME = "*";
	public static final String VERSION = "@VERSION@";

	public static PacketRegistry packet;

	@SidedProxy(clientSide = "pl.asie.charset.tweaks.ProxyClient", serverSide = "pl.asie.charset.tweaks.ProxyCommon")
	public static pl.asie.charset.tweaks.ProxyCommon proxy;
	
	private Configuration configuration;

	private final Set<Tweak> tweakSet = new HashSet<Tweak>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		tweakSet.add(new TweakAutoReplace());
		tweakSet.add(new TweakDoubleDoors());
		tweakSet.add(new TweakDyeableMinecarts());
		tweakSet.add(new TweakGraphite());
        tweakSet.add(new TweakImprovedNoteblock());
        tweakSet.add(new TweakMobControl());
        tweakSet.add(new TweakNoSprinting());

		configuration = new Configuration(ModCharsetLib.instance.getConfigFile("tweaks.cfg"));

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

		for (Tweak t : tweakSet) {
			if (t.isEnabled()) {
                if (!t.init()) {
                    ModCharsetLib.logger.error("Tweak " + t.getClass().getSimpleName() + " failed to load! Please disable it in the config.");
                    tweakSet.remove(t);
                }
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
