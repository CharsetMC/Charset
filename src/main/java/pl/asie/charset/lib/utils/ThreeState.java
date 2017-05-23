package pl.asie.charset.lib.utils;

public enum ThreeState {
    YES,
    MAYBE,
    NO;

    public ThreeState or(ThreeState other) {
        if (this == YES || other == YES)
            return YES;
        else if (this == NO || other == NO)
            return NO;
        else
            return MAYBE;
    }

    public boolean matches(boolean value) {
        return this == MAYBE || ((this == YES) == value);
    }
}
