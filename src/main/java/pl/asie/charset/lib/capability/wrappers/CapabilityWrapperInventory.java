/*
 * Copyright (c) 2015, 2016, 2017 Adrian Siekierka
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

package pl.asie.charset.lib.capability.wrappers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import pl.asie.charset.lib.capability.CapabilityHelper;

/**
 * Created by asie on 11/29/16.
 */
public class CapabilityWrapperInventory implements CapabilityHelper.Wrapper<IItemHandler> {
	@Override
	public IItemHandler get(ICapabilityProvider provider, EnumFacing facing) {
		if (provider instanceof ISidedInventory) {
			IItemHandler handler = new SidedInvWrapper((ISidedInventory) provider, facing);
			return handler.getSlots() > 0 ? handler : null;
		} else if (provider instanceof IInventory) {
			return new InvWrapper((IInventory) provider);
		} else {
			return null;
		}
	}
}
