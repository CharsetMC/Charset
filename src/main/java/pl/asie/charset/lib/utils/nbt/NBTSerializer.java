package pl.asie.charset.lib.utils.nbt;

import net.minecraft.nbt.NBTBase;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public final class NBTSerializer {
    public static final NBTSerializer INSTANCE = new NBTSerializer();

    private final Map<Class, Class> shortcuts = new IdentityHashMap<>();
    private final Map<Class, Function<Object, ? extends NBTBase>> serializers = new IdentityHashMap<>();
    private final Map<Class, Function<? extends NBTBase, Object>> deserializers = new IdentityHashMap<>();
    private final Map<Class, Integer> nbtTypes = new IdentityHashMap<>();

    private NBTSerializer() {

    }

    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> tClass, int tagType, Function<T, ? extends NBTBase> serializer, Function<? extends NBTBase, T> deserializer) {
        serializers.put(tClass, (Function<Object, ? extends NBTBase>) serializer);
        deserializers.put(tClass, (Function<? extends NBTBase, Object>) deserializer);
    }
}
