package pl.asie.charset.lib.utils;

public final class MiscUtils {
	private static final char[] hexArray = "0123456789abcdef".toCharArray();

	private MiscUtils() {

	}

	public static String asHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];

		for (int j = 0; j < bytes.length; ++j) {
			int v = bytes[j] & 255;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 15];
		}

		return new String(hexChars);
	}
}
