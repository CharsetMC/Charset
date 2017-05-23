package pl.asie.charset.module.audio.transport;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;

@CharsetModule(
        name = "audio.transport",
        description = "Audio cables",
        dependencies = {"lib.wires"}
)
public class CharsetAudioTransport {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        RegistryUtils.register(new WireProviderAudioCable(), "audio_cable");
    }
}
