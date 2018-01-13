package pl.asie.charset.module.tablet.format.api;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.concurrent.Future;

/**
 * Called in a separate thread, so you better be prepared for this.
 */
public interface IRouter {
	@Nullable
	String get(URI path);

	boolean matches(URI path);
}
