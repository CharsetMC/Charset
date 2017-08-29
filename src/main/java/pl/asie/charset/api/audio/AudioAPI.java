package pl.asie.charset.api.audio;

import pl.asie.charset.api.lib.ISimpleInstantiatingRegistry;

// Use CharsetAPI.findSimpleInstantiatingRegistry
public abstract class AudioAPI {
    @Deprecated
    public static ISimpleInstantiatingRegistry<AudioData> DATA_REGISTRY;
    @Deprecated
    public static ISimpleInstantiatingRegistry<AudioSink> SINK_REGISTRY;
}
