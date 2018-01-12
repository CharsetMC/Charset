package pl.asie.charset.patchwork;

import pl.asie.charset.ModCharset;

import java.lang.reflect.Field;

public final class PatchworkHelper {
	private static final Class c;
	private static boolean sentPatchesMsg = false;

	static {
		Class cc;
		try {
			cc = Class.forName("pl.asie.charset.patches.CharsetPatchwork");
		} catch (Exception e) {
			cc = null;
		}
		c = cc;
	}

	private PatchworkHelper() {

	}

	private static void sendPatchesMsgIfNotSent() {
		if (!sentPatchesMsg) {
			sentPatchesMsg = true;
			if (c != null) {
				ModCharset.logger.info("CharsetPatches present!");
			} else {
				ModCharset.logger.info("CharsetPatches not present!");
			}
		}
	}

	public static boolean getBoolean(String name) {
		sendPatchesMsgIfNotSent();

		if (c == null) {
			return false;
		} else {
			try {
				Field f = c.getDeclaredField(name);
				f.setAccessible(true);
				return f.getBoolean(null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
}
