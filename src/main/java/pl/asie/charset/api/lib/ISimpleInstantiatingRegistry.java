package pl.asie.charset.api.lib;

import java.util.function.Supplier;

public interface ISimpleInstantiatingRegistry<T> {
    boolean register(Class<? extends T> clazz, Supplier<T> supplier);
    int getId(T object);
    Supplier<T> get(int id);
    T create(int id);
}
