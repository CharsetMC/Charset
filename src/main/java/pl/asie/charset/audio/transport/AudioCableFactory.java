package pl.asie.charset.audio.transport;

import mcmultipart.multipart.IMultipart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pl.asie.charset.lib.wires.PartWire;
import pl.asie.charset.lib.wires.WireFactory;

public class AudioCableFactory extends WireFactory {
    @Override
    public PartWire createPart(ItemStack stack) {
        return new PartAudioCable().setFactory(this);
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
        return new ResourceLocation("charsetaudio:blocks/audio_cable");
    }

    @Override
    public IMultipart createPart(ResourceLocation type, boolean client) {
        return new PartAudioCable().setFactory(this);
    }
}
