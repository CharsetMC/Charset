package pl.asie.charset.audio;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import pl.asie.charset.audio.util.AudioStreamManager;

@Mod(modid = ModCharsetAudio.MODID, name = ModCharsetAudio.NAME, version = ModCharsetAudio.VERSION)
public class ModCharsetAudio {
	public static final String MODID = "CharsetAudio";
	public static final String NAME = "♫";
	public static final String VERSION = "0.1.0";

	@SidedProxy(clientSide = "pl.asie.charset.audio.client.AudioStreamManagerClient", serverSide = "pl.asie.charset.audio.util.AudioStreamManager")
	public static AudioStreamManager audio;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
	}
}
