/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.module.tablet.format.api;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

public class TabletAPIClient {
	public static final TabletAPIClient INSTANCE = new TabletAPIClient();

	protected TabletAPIClient() {
	}

	private final Map<Class<? extends Word>, WordPrinterMinecraft> printerMinecraftMap = new IdentityHashMap<>();

	public void registerPrinterMinecraft(Class<? extends Word> c, WordPrinterMinecraft<? extends Word> printer) {
		printerMinecraftMap.put(c, printer);
	}

	@SuppressWarnings("unchecked")
	public WordPrinterMinecraft getPrinterMinecraft(Word w) {
		Class c = w.getClass();
		while (c != Word.class && c != Object.class && c != null && !printerMinecraftMap.containsKey(c)) {
			c = c.getSuperclass();
		}
		return printerMinecraftMap.get(c);
	}
}
