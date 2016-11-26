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

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

public final class ColorUtils {
	private static final int[] WOOL_TO_RGB = new int[]{
			0xFAFAFA, 0xD87F33, 0xB24CD8, 0x6699D8,
			0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C,
			0x999999, 0x4C7F99, 0x7F3FB2, 0x334CB2,
			0x664C33, 0x667F33, 0x993333, 0x191919
	};

	private static final String[] UPPERCASE_DYE_SUFFIXES = new String[]{
			"White", "Orange", "Magenta", "LightBlue",
			"Yellow", "Lime", "Pink", "Gray",
			"LightGray", "Cyan", "Purple", "Blue",
			"Brown", "Green", "Red", "Black"
	};

	private static final String[] LOWERCASE_DYE_SUFFIXES = new String[]{
			"white", "orange", "magenta", "lightBlue",
			"yellow", "lime", "pink", "gray",
			"lightGray", "cyan", "purple", "blue",
			"brown", "green", "red", "black"
	};

	private static final int[] OREDICT_DYE_IDS = new int[16];

	private static final char[] WOOL_TO_CHAT = new char[]{
			'f', '6', 'd', '9', 'e', 'a', 'd', '8',
			'7', '3', '5', '1', '6', '2', '4', '0'
	};

	private ColorUtils() {

	}

	public static void initialize() {
		for (int i = 0; i < 16; i++) {
			OREDICT_DYE_IDS[i] = OreDictionary.getOreID("dye" + UPPERCASE_DYE_SUFFIXES[i]);
		}
	}

	public static int getColorIDFromDye(ItemStack stack) {
		if (stack.isEmpty()) {
			return -1;
		}

		if (stack.getItem() == Items.DYE) {
			return 15 - stack.getItemDamage();
		}

		int[] itemOreIDs = OreDictionary.getOreIDs(stack);
		for (int i = 0; i < 16; i++) {
			for (int id : itemOreIDs) {
				if (OREDICT_DYE_IDS[i] == id) {
					return i;
				}
			}
		}

		return -1;
	}

	public static boolean isDye(ItemStack stack) {
		return getColorIDFromDye(stack) >= 0;
	}

	public static int getRGBColor(int wool) {
		return WOOL_TO_RGB[wool & 15];
	}

	public static String getOreDictEntry(String prefix, int wool) {
		return prefix + UPPERCASE_DYE_SUFFIXES[wool & 15];
	}

	public static String getLangEntry(String prefix, int wool) {
		return prefix + LOWERCASE_DYE_SUFFIXES[wool & 15];
	}

	public static String ampersandToColor(String chat) {
		return chat.replaceAll("&(?=[0-9A-FK-ORa-fk-or])", "\u00a7");
	}

	public static String stripColor(String chat) {
		return chat.replaceAll("[&§][0-9A-FK-ORa-fk-or]", "");
	}
}
