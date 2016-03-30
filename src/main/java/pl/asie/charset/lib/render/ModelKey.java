package pl.asie.charset.lib.render;

public class ModelKey<T extends IRenderComparable<T>> {
    T object;
    Class objectClass;

    public ModelKey(T object) {
        this.object = object;
        this.objectClass = object.getClass();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !objectClass.isAssignableFrom(other.getClass())) {
            return false;
        }

        return object.renderEquals((T) other);
    }

    @Override
    public int hashCode() {
        return object.renderHashCode();
    }
}
