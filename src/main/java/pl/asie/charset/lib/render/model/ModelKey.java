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

package pl.asie.charset.lib.render.model;

import net.minecraft.util.BlockRenderLayer;

import javax.annotation.Nonnull;

public class ModelKey<T extends IRenderComparable<T>> {
    T object;
    BlockRenderLayer layer;
    Class objectClass;

    public ModelKey(@Nonnull T object, @Nonnull BlockRenderLayer layer) {
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
