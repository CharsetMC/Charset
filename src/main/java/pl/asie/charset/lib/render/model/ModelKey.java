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

package pl.asie.charset.lib.render.model;

import net.minecraft.util.BlockRenderLayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelKey<T extends IRenderComparable<T>> {
    T object;
    BlockRenderLayer layer;
    Class objectClass;

    public ModelKey(@Nonnull T object, @Nullable BlockRenderLayer layer) {
        this.object = object;
        this.layer = layer;
        this.objectClass = object.getClass();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ModelKey) ){
            return false;
        }

        if (layer != ((ModelKey) other).layer) {
            return false;
        }

        IRenderComparable o = ((ModelKey) other).object;

        if (!objectClass.isInstance(o)) {
            return false;
        }

        return object.renderEquals((T) o);
    }

    @Override
    public int hashCode() {
        if (layer == null) {
            return object.renderHashCode() * 3;
        } else {
            return object.renderHashCode() * 3 + layer.ordinal() + 1;
        }
    }
}
