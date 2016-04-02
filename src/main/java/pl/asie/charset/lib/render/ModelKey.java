package pl.asie.charset.lib.render;

import javax.annotation.Nonnull;

public class ModelKey<T extends IRenderComparable<T>> {
    T object;
    Class objectClass;

    public ModelKey(@Nonnull T object) {
        this.object = object;
        this.objectClass = object.getClass();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof ModelKey) ){
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
        return object.renderHashCode();
    }
}
