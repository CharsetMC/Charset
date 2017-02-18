package pl.asie.charset.lib.utils;

import java.util.function.Supplier;

public final class FunctionalUtils {
    private FunctionalUtils() {

    }

    public static <T> Supplier<T> lazySupplier(Supplier<T> creator) {
        return new Supplier<T>() {
            private T instance;

            @Override
            public T get() {
                return instance == null ? (instance = creator.get()) : instance;
            }
        };
    }
}
