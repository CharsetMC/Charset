/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.utils;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedPropertyGeneric<T> implements IUnlistedProperty<T> {
    private final String name;
    private final Class<T> klazz;

    public UnlistedPropertyGeneric(String name, Class<T> klazz) {
        this.name = name;
        this.klazz = klazz;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(T value) {
        return true;
    }

    @Override
    public Class<T> getType() {
        return klazz;
    }

    @Override
    public String valueToString(T value) {
        return value != null ? value.toString() : "null";
    }
}
