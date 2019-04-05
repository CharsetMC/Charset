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

package pl.asie.charset.lib.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import pl.asie.charset.api.locks.Lockable;
import pl.asie.charset.lib.capability.Capabilities;

import javax.annotation.Nullable;

public class TraitLockable extends Trait {
	private final Lockable lockable;

	public TraitLockable(TileEntity owner) {
		super();
		this.lockable = new Lockable(owner);
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		if (!isClient) {
			lockable.deserializeNBT(compound);
		}
	}

	@Override
	public NBTTagCompound writeNBTData(boolean isClient) {
		return isClient ? new NBTTagCompound() : lockable.serializeNBT();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == Capabilities.LOCKABLE;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == Capabilities.LOCKABLE
				? Capabilities.LOCKABLE.cast(lockable)
				: null;
	}

	public Lockable get() {
		return lockable;
	}
}
