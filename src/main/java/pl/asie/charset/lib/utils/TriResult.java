package pl.asie.charset.lib.utils;

import pl.asie.charset.lib.CharsetIMC;

public enum TriResult {
    YES,
    MAYBE,
    NO;

    public TriResult or(TriResult other) {
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
