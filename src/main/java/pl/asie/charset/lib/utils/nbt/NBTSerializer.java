/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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
