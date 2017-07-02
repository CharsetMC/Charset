package pl.asie.charset.module.audio.transport;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.wires.WireProvider;

@CharsetModule(
        name = "audio.transport",
        description = "Audio cables",
        dependencies = {"lib.wires"},
        profile = ModuleProfile.TESTING
)
public class CharsetAudioTransport {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @SubscribeEvent
    public void registerWires(RegistryEvent.Register<WireProvider> event) {
        RegistryUtils.register(event.getRegistry(), new WireProviderAudioCable(), "audio_cable");
    }
}
