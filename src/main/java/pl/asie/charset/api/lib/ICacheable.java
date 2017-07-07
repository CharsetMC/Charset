package pl.asie.charset.api.lib;

/**
 * Please put this interface on those of your capabilities which can
 * safely be cached. The interface has to be *on* the capability object,
 * *not* as a separate capability!
 */
public interface ICacheable {
    /**
     * Returns true for the duration of the object being valid to cache.
     * Please note that the parent object's validity comes first - that
     * is, if the tile entity is invalid, all the capabilities provided
     * by said tile entity are also considered invalid.
     *
     * The default implementation always returns true to make life
     * easier for the most common use-case of capabilities - objects
     * created at the beginning of the object's lifetime and valid
     * throughout the duration of said lifetime.
     *
     * @return Whether the cached object is valid.
     */
    default boolean isCacheValid() {
        return true;
    }
}



