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

package pl.asie.charset.lib.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import pl.asie.charset.lib.utils.ItemUtils;

import javax.annotation.Nullable;

public abstract class TraitItemHolder extends Trait {
	private ItemStackHandler handler = new ItemStackHandler(1) {
		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			TraitItemHolder.this.onContentsChanged();
		}
	};

	public void onContentsChanged() {

	}

	public abstract boolean isStackAllowed(ItemStack stack);

	public abstract EnumFacing getTop();

	public ItemStack getStack() {
		return handler.getStackInSlot(0);
	}

	public IItemHandler getHandler() {
		return handler;
	}

	@Override
	public void readNBTData(NBTTagCompound compound, boolean isClient) {
		handler.deserializeNBT(compound);
	}

	@Override
	public NBTTagCompound writeNBTData(boolean isClient) {
		return handler.serializeNBT();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != getTop();
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
		} else {
			return null;
		}
	}

	public boolean activate(TileBase parent, EntityPlayer player, EnumFacing side, EnumHand hand) {
		if (side == getTop() && hand == EnumHand.MAIN_HAND) {
			if (!getStack().isEmpty()) {
				ItemUtils.spawnItemEntity(parent.getWorld(),
						new Vec3d(parent.getPos()).addVector(0.5F, 0.5F, 0.5F).add(new Vec3d(getTop().getDirectionVec()).scale(0.5F)),
						getStack(), 0, 0, 0, 0
				);
				setStack(ItemStack.EMPTY);
				return true;
			} else {
				ItemStack held = player.getHeldItem(hand);
				if (!held.isEmpty() && isStackAllowed(held)) {
					setStack(held.splitStack(1));
					return true;
				}
			}
		}

		return false;
	}

	public void setStack(ItemStack stack) {
		handler.setStackInSlot(0, stack);
	}
}
