package pl.asie.charset.lib.loader;

public enum ModuleProfile {
    STABLE,
    TESTING,
    UNSTABLE,
    VERY_UNSTABLE,
    COMPAT;

    public boolean includes(ModuleProfile other) {
        if (other == VERY_UNSTABLE) {
            return this == VERY_UNSTABLE;
        } else if (other == COMPAT) {
            return true;
        } else if (this == COMPAT) {
            return false;
        } else if (this == VERY_UNSTABLE) {
            return true;
        } else {
            return this.ordinal() >= other.ordinal();
        }
    }
}
