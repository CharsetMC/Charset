package pl.asie.charset.api.audio;

import pl.asie.charset.api.lib.ISimpleInstantiatingRegistry;

public abstract class AudioAPI {
    public static ISimpleInstantiatingRegistry<AudioData> DATA_REGISTRY;
    public static ISimpleInstantiatingRegistry<AudioSink> SINK_REGISTRY;
}
