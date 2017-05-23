package pl.asie.charset.module.audio.transport;

import net.minecraft.util.ResourceLocation;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.lib.wires.IWireContainer;
import pl.asie.charset.lib.wires.Wire;
import pl.asie.charset.lib.wires.WireProvider;

public class WireProviderAudioCable extends WireProvider {
    @Override
    public Wire create(IWireContainer container, WireFace location) {
        return new WireAudioCable(container, this, location);
    }

    @Override
    public float getWidth() {
        return 0.375f;
    }

    @Override
    public float getHeight() {
        return 0.1875f;
    }

    @Override
    public ResourceLocation getTexturePrefix() {
        return new ResourceLocation("charset:blocks/audio_cable/");
    }
}
