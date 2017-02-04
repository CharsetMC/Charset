package pl.asie.charset.lib.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Use this annotation on methods which should be called in the PostInit phase
 * if a specific mod is loaded. Useful for providing self-contained
 * compatibility code.
 */
@Retention(value = RUNTIME)
@Target(value = METHOD)
public @interface ModCompatProvider {
	/**
	 * @return The mod ID of the mod providing compatibility with.
	 */
	String value();
}
