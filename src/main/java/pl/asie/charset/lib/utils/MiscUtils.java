/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
