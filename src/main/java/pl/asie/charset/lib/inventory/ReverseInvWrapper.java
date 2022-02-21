/*
 * Copyright (c) 2015, 2016, 2017, 2018, 2019, 2020 Adrian Siekierka
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

package pl.asie.charset.lib.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ReverseInvWrapper implements IInventory {
	private final IItemHandlerModifiable parent;

	public ReverseInvWrapper(IItemHandlerModifiable parent) {
		this.parent = parent;
	}

	@Override
	public int getSizeInventory() {
		return this.parent.getSlots();
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < this.parent.getSlots(); i++) {
			ItemStack stack = this.parent.getStackInSlot(i);
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return this.parent.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return this.parent.extractItem(index, count, false);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return this.parent.extractItem(index, getInventoryStackLimit(), false);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.parent.setStackInSlot(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return Items.AIR.getItemStackLimit();
	}

	@Override
	public void markDirty() {

	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return this.parent.isItemValid(index, stack);
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	public String getName() {
		return "ReverseInvWrapper";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}
}
