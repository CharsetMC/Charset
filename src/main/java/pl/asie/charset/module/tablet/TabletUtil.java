package pl.asie.charset.module.tablet;

import com.google.common.base.Charsets;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public final class TabletUtil {
	private TabletUtil() {

	}

	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, Charsets.UTF_8.name()).replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, Charsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
