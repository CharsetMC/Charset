/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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
import org.apache.commons.lang3.tuple.Triple;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class TabletAPI {
	public static final TabletAPI INSTANCE = new TabletAPI();

	protected TabletAPI() {
		service = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
	}

	private final Map<String, ICommand> commandMap = new HashMap<>();
	private final Table<TextPrinterFormat, Class<? extends Word>, WordPrinterText> textOutputPrinterTable = HashBasedTable.create();
	private final Table<TextPrinterFormat, Class<? extends IStyle>, StylePrinterText> styleOutputPrinterTable = HashBasedTable.create();
	private final List<Pair<String, String>> books = new ArrayList<>();
	private final List<IRouter> routes = new ArrayList<>();
	private final ExecutorService service;

	public void addBook(String name, String url) {
		books.add(Pair.of(name, url));
	}

	public Collection<Pair<String, String>> getBooks() {
		return books;
	}

	public void registerRouter(IRouter route) {
		routes.add(route);
	}

	public Collection<IRouter> getRouters() {
		return Collections.unmodifiableCollection(routes);
	}

	public boolean matchesRoute(final URI path) {
		for (IRouter routerPair : this.routes) {
			if (routerPair.matches(path)) {
				return true;
			}
		}

		return false;
	}

	public Future<String> getRoute(final URI path) {
		List<IRouter> routers = new ArrayList<>();
		for (IRouter routerPair : this.routes) {
			if (routerPair.matches(path)) {
				routers.add(routerPair);
			}
		}

		return service.submit(() -> {
			for (IRouter router : routers) {
				String out = router.get(path);
				if (out != null) {
					return out;
				}
			}

			return null;
		});
	}

	public void registerPrinterStyle(TextPrinterFormat format, Class<? extends IStyle> c, StylePrinterText<? extends IStyle> printerText) {
		styleOutputPrinterTable.put(format, c, printerText);
	}

	@SuppressWarnings("unchecked")
	public StylePrinterText getPrinterStyle(TextPrinterFormat format, IStyle w) {
		Class c = w.getClass();
		while (c != IStyle.class && c != Object.class && c != null && !styleOutputPrinterTable.contains(format, c)) {
			c = c.getSuperclass();
		}
		return styleOutputPrinterTable.get(format, c);
	}

	public void registerPrinterText(TextPrinterFormat format, Class<? extends Word> c, WordPrinterText<? extends Word> printerText) {
		textOutputPrinterTable.put(format, c, printerText);
	}

	@SuppressWarnings("unchecked")
	public WordPrinterText getPrinterText(TextPrinterFormat format, Word w) {
		Class c = w.getClass();
		while (c != Word.class && c != Object.class && c != null && !textOutputPrinterTable.contains(format, c)) {
			c = c.getSuperclass();
		}
		return textOutputPrinterTable.get(format, c);
	}

	public ICommand getCommand(String name) {
		return commandMap.get(name);
	}

	public void registerCommand(String name, ICommand command) {
		commandMap.put(name, command);
	}
}
