package pl.asie.charset.api.audio;

import net.minecraft.entity.Entity;

public interface IAudioSinkEntity extends IAudioSink {
    Entity getEntity();
}
