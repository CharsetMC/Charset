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

package pl.asie.charset.module.tools.engineering;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.item.ItemBase;

public class ItemSignalMeter extends ItemBase {
	public ItemSignalMeter() {
		super();
		setMaxStackSize(1);
		setTranslationKey("charset.signal_meter");
	}

	public static class Color implements IItemColor {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			return tintIndex == 1 ? 0xFFEDE740 : -1;
		}
	}
}
