package pl.asie.charset.audio.transport;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.wires.WireManager;

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
