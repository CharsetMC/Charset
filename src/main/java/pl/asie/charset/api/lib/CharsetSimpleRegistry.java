package pl.asie.charset.api.lib;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asie on 6/12/16.
 */
public class CharsetSimpleRegistry<T> {
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
            throw new IllegalStateException(String.format("Tried to access unregistered ID %d!", id));
        }
        return LIST.get(id);
    }

    public T create(int id) {
        try {
            return get(id).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
