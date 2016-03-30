package pl.asie.charset.lib.render;

public interface IRenderComparable<T> {
    boolean renderEquals(T other);
    int renderHashCode();
}
