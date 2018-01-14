/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of Charset.
 *
 * Charset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Charset.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.charset.lib.utils;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import pl.asie.charset.api.lib.ISimpleInstantiatingRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CharsetSimpleInstantiatingRegistry<T> implements ISimpleInstantiatingRegistry<T> {
    public final List<Supplier<T>> LIST = new ArrayList<>();
    public final TObjectIntMap<Class<? extends T>> MAP = new TObjectIntHashMap<>();

    @Override
    public boolean register(Class<? extends T> clazz, Supplier<T> supplier) {
        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(String.format("Tried to register class with no empty constructor %s!", clazz.getName()));
        }
        MAP.put(clazz, LIST.size());
        return LIST.add(supplier);
    }

    @Override
    public int getId(T object) {
        return MAP.get(object.getClass());
    }

    @Override
    public Supplier<T> get(int id) {
        if (id < 0 || LIST.size() <= id) {
            throw new IllegalStateException(String.format("Tried to access unregistered ID %d!", id));
        }
        return LIST.get(id);
    }

    @Override
    public T create(int id) {
        try {
            return get(id).get();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
