/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.loader;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public abstract class AnnotatedPluginHandler<T> {
    private final Class annotationClass;
    private Set<T> plugins = null;

    protected AnnotatedPluginHandler(Class annotationClass) {
        this.annotationClass = annotationClass;
    }

    @SuppressWarnings("unchecked")
    public Set<T> getPlugins() {
        if (plugins == null) {
            ImmutableSet.Builder<T> builder = new ImmutableSet.Builder<>();

            for (String s : ModuleLoader.classNames.get(annotationClass)) {
                try {
                    T plugin = (T) Class.forName(s).newInstance();
                    builder.add(plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            plugins = builder.build();
        }
        return plugins;
    }

}
