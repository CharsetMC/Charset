/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(value = RUNTIME)
@Target(value = TYPE)
public @interface CharsetModule {
	String name();
	ModuleProfile profile();
	String description() default "";
	String moduleConfigGui() default "";
	boolean isVisible() default true;
	boolean isDefault() default true;
	boolean isClientOnly() default false;
	boolean isServerOnly() default false;
	String[] categories() default {};
	String[] dependencies() default {};
	String[] antidependencies() default {};

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
