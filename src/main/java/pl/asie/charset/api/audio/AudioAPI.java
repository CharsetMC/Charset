package pl.asie.charset.api.audio;

import pl.asie.charset.api.lib.CharsetSimpleRegistry;

public class AudioAPI {
    public static final CharsetSimpleRegistry<AudioData> DATA_REGISTRY = new CharsetSimpleRegistry<AudioData>();
    public static final CharsetSimpleRegistry<AudioSink> SINK_REGISTRY = new CharsetSimpleRegistry<AudioSink>();
}
