/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.charset.api.lib;

/**
 * This capability is provided by Charset items which are dyeable, but you are
 * welcome to utilize it yourself. For said items, Charset provides a simple
 * recipe framework (one .json to add a color-mixing, dyeing recipe, as well
 * as cauldron-based washing).
 */
public interface IDyeableItem {
	default int getColorSlotCount() {
		return 1;
	}

	int getColor(int slot);
	boolean hasColor(int slot);
	boolean removeColor(int slot);
	boolean setColor(int slot, int color);
}
