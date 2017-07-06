package pl.asie.charset.lib.utils;

public final class MathUtils {
    private MathUtils() {

    }

    public static float interpolate(float a, float b, float amount) {
        return (a * (1.0f - amount) + b * amount);
    }
}
