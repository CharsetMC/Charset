package pl.asie.charset.api.audio;

import java.util.Collection;

public interface IAudioModifier {
    Collection<String> getProperties();
    Object applyProperty(String key, Object original);
}
