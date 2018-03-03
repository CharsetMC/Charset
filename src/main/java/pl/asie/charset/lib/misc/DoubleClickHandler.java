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

package pl.asie.charset.lib.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pl.asie.charset.lib.CharsetLib;

import java.util.WeakHashMap;

public class DoubleClickHandler {
	private final WeakHashMap<EntityPlayer, Long> lastClickMap = new WeakHashMap<>();

	public DoubleClickHandler() {

	}

	public void markLastClick(EntityPlayer player) {
		lastClickMap.put(player, player.getEntityWorld().getTotalWorldTime());
	}

	public boolean isDoubleClick(EntityPlayer player) {
		Long lastClick = lastClickMap.get(player);
		return lastClick != null && player.getEntityWorld().getTotalWorldTime() - lastClick < CharsetLib.doubleClickDuration;
	}

}
