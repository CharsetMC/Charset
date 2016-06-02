package pl.asie.charset.api.audio;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IAudioModifierContainer {
    @Nullable Collection<IAudioModifier> getModifiers();
}
