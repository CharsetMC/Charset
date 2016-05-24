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

public class ChatUtils {
	private static int[] dyeToChatArray = {
		0, 4, 2, 0, 1, 5, 3, 7, 8, 13, 10, 14, 9, 5, 6, 15
	};
	
	public static String color(String chat) {
		return chat.replaceAll("&(?=[0-9A-FK-ORa-fk-or])", "\u00a7");
	}

	public static String stripColors(String chat) {
		return chat.replaceAll("[&§][0-9A-FK-ORa-fk-or]", "");
	}
	
	public static int dyeToChat(int dyeColor) {
		return dyeToChatArray[dyeColor % 16];
	}
	
	public static int woolToChat(int woolColor) {
		return dyeToChat(15 - woolColor);
	}
}
