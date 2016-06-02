package pl.asie.charset.lib.utils;

public final class MiscUtils {
    private MiscUtils() {

    }

    public static boolean contains(int[] array, int value) {
        if (array != null) {
            for (int i : array) {
                if (i == value) {
                    return true;
                }
            }
        }

        return false;
    }
}
