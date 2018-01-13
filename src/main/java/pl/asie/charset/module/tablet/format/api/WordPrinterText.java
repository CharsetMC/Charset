package pl.asie.charset.module.tablet.format.api;

@FunctionalInterface
public interface WordPrinterText<T extends Word> {
	String output(T word);
}
