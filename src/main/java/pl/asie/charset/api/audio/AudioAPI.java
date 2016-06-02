package pl.asie.charset.api.audio;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioAPI {
    public static class Registry<T> {
        public final List<Class<? extends T>> LIST = new ArrayList<Class<? extends T>>();
        public final TObjectIntMap<Class<? extends T>> MAP = new TObjectIntHashMap<Class<? extends T>>();

        public boolean register(Class<? extends T> clazz) {
            try {
                clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(String.format("Tried to register class with no empty constructor %s!", clazz.getName()));
            }
            MAP.put(clazz, LIST.size());
            return LIST.add(clazz);
        }

        public int getId(Class<? extends T> clazz) {
            return MAP.get(clazz);
        }

        public Class<? extends T> get(int id) {
            if (id < 0 || LIST.size() <= id) {
                throw new IllegalStateException(String.format("Tried to access unregistered audio packet ID %d!", id));
            }
            return LIST.get(id);
        }
    }

    public static final Registry<AudioPacket> PACKET_REGISTRY = new Registry<AudioPacket>();
    public static final Registry<AudioSink> SINK_REGISTRY = new Registry<AudioSink>();
}
