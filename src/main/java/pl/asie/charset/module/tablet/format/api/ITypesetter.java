package pl.asie.charset.module.tablet.format.api;

public interface ITypesetter {
    boolean hasFixedWidth();
    int getWidth();

    void pushStyle(IStyle... style) throws TruthError;
    void popStyle(int count) throws TruthError;

    default void popStyle() throws TruthError {
        popStyle(1);
    }

    void write(String text) throws TruthError;
    void write(Word word) throws TruthError;
}
