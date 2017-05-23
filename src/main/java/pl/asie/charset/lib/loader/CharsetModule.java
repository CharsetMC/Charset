package pl.asie.charset.lib.loader;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(value = RUNTIME)
@Target(value = TYPE)
public @interface CharsetModule {
	String name();
	String description() default "";
	boolean isVisible() default true;
	boolean isDefault() default true;
	boolean isDevOnly() default false;
	boolean isClientOnly() default false;
	boolean isServerOnly() default false;
	boolean isModCompat() default false;
	String[] dependencies() default {};

	@Retention(value = RUNTIME)
	@Target(value = FIELD)
	@interface PacketRegistry {
		String value() default "";
	}

	@Retention(value = RUNTIME)
	@Target(value = FIELD)
	@interface Instance {
		String value() default "";
	}

	@Retention(value = RUNTIME)
	@Target(value = FIELD)
	@interface Configuration {
		String value() default "";
	}
}
