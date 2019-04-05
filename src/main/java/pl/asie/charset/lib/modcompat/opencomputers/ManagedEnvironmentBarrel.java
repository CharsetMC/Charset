/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019 Adrian Siekierka
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

package pl.asie.charset.lib.modcompat.opencomputers;

import li.cil.oc.api.API;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import pl.asie.charset.api.storage.IBarrel;

/**
 * This mimics the DSU interface used by OpenComputers in 1.7.10.
 */
public class ManagedEnvironmentBarrel extends CharsetManagedEnvironment {
	private final IBarrel barrel;

	public ManagedEnvironmentBarrel(IBarrel barrel) {
		super("barrel", Visibility.Network, 0);
		this.barrel = barrel;
	}

	@Callback(doc = "function():int -- Get the maximum number of stored items.")
	public Object[] getMaxStoredCount(Context context, Arguments args) {
		return new Object[] { barrel.getMaxItemCount() };
	}

	@Callback(doc = "function():int -- Get the number of currently stored items.")
	public Object[] getStoredCount(Context context, Arguments args) {
		return new Object[] { barrel.getItemCount() };
	}

	@Callback(doc = "function():int -- Get the currently stored item type.")
	public Object[] getStoredItemType(Context context, Arguments args) {
		if (API.config.getBoolean("misc.allowItemStackInspection")) {
			return new Object[] { barrel.extractItem(1, true) };
		} else {
			return new Object[] { null, "permission denied" };
		}
	}
}
