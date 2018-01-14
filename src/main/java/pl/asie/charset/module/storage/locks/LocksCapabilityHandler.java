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

package pl.asie.charset.module.storage.locks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.api.locks.Lockable;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.config.ConfigUtils;
import pl.asie.charset.patchwork.LocksCapabilityHook.Handler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class LocksCapabilityHandler implements Handler {
	private static final Set<Capability> FORBIDDEN = Collections.newSetFromMap(new IdentityHashMap<>());

	public static void addCapability(Capability capability, boolean def) {
		if (ConfigUtils.getBoolean(CharsetStorageLocks.config, "blockedCapabilities", capability.getName(), def, null, true)) {
			FORBIDDEN.add(capability);
		}
	}

	@Override
	public boolean blocksCapability(TileEntity tile, Capability capability, EnumFacing facing) {
		if (capability == Capabilities.LOCKABLE) {
			// Early return to prevent recursion
			return false;
		} else {
			if (tile.hasCapability(Capabilities.LOCKABLE, facing)) {
				Lockable lockable = tile.getCapability(Capabilities.LOCKABLE, facing);
				if (lockable != null && lockable.hasLock()) {
					return FORBIDDEN.contains(capability);
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
}
