package pl.asie.charset.api.audio;

import java.util.HashSet;
import java.util.Set;

public abstract class AudioPacket {
    protected Set<IAudioSink> sinks = new HashSet<IAudioSink>();

    public boolean add(IAudioSink sink) {
        return sinks.add(sink);
    }

    public int sinkCount() {
        return sinks.size();
    }

    public abstract void beginPropagation();
    public abstract void endPropagation();
}
