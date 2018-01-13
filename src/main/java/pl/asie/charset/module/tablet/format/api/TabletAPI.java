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
	private final Map<Class<? extends Word>, WordPrinterMinecraft> printerMinecraftMap = new IdentityHashMap<>();
	private final Table<TextPrinterFormat, Class<? extends Word>, WordPrinterText> textOutputPrinterTable = HashBasedTable.create();
	private final List<IRouter> routes = new ArrayList<>();
	private final ExecutorService service;

	public void registerRouter(IRouter route) {
		routes.add(route);
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

	public <T extends Word> void registerPrinterMinecraft(Class<T> c, WordPrinterMinecraft<T> printer) {
		printerMinecraftMap.put(c, printer);
	}

	public <T extends Word> void registerPrinterText(TextPrinterFormat format, Class<T> c, WordPrinterText<T> printerText) {
		textOutputPrinterTable.put(format, c, printerText);
	}

	@SuppressWarnings("unchecked")
	public WordPrinterText getPrinterText(TextPrinterFormat format, Word w) {
		Class c = w.getClass();
		while (c != Word.class && !textOutputPrinterTable.contains(format, c)) {
			c = c.getSuperclass();
		}
		return textOutputPrinterTable.get(format, c);
	}

	@SuppressWarnings("unchecked")
	public WordPrinterMinecraft getPrinterMinecraft(Word w) {
		Class c = w.getClass();
		while (c != Word.class && !printerMinecraftMap.containsKey(c)) {
			c = c.getSuperclass();
		}
		return printerMinecraftMap.get(c);
	}

	public ICommand getCommand(String name) {
		return commandMap.get(name);
	}

	public void registerCommand(String name, ICommand command) {
		commandMap.put(name, command);
	}
}
