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

package pl.asie.charset.lib.modcompat.opencomputers;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import pl.asie.charset.lib.capability.CapabilityHelper;

import java.util.function.Function;
import java.util.function.Supplier;

public class DriverCapability<T> implements DriverBlock {
	private final Capability<T> capability;
	private final Function<T, ManagedEnvironment> supplier;

	public DriverCapability(Capability<T> capability, Function<T, ManagedEnvironment> supplier) {
		this.capability = capability;
		this.supplier = supplier;
	}

	protected void register() {
		if (capability != null) {
			Driver.add(this);
		}
	}

	@Override
	public boolean worksWith(World world, BlockPos blockPos, EnumFacing enumFacing) {
		return CapabilityHelper.has(world, blockPos, capability, enumFacing.getOpposite(),
				true, true, false);
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos blockPos, EnumFacing enumFacing) {
		T object = CapabilityHelper.get(world, blockPos, capability, enumFacing.getOpposite(),
				true, true, false);
		if (object != null) {
			return supplier.apply(object);
		} else {
			return null;
		}
	}
}
