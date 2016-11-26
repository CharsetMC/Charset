/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.lib.inventory;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;

public class InventorySimple implements IInventory {
	public final Set<EntityPlayer> watchers;
	private final IInventoryOwner owner;
	private final int size;
	private final ItemStack[] items;

	public InventorySimple(int size, IInventoryOwner owner) {
		this.owner = owner;
		this.size = size;
		this.items = new ItemStack[size];
		this.watchers = new HashSet<EntityPlayer>();
		for (int i = 0; i < size; i++)
			items[i] = ItemStack.EMPTY;
	}

	public void readFromNBT(NBTTagCompound nbt, String key) {
		NBTTagList itemList = nbt.getTagList(key, 10);
		for (int i = 0; i < Math.min(size, itemList.tagCount()); i++) {
			NBTTagCompound cpd = itemList.getCompoundTagAt(i);
			items[i] = new ItemStack(cpd);
		}
	}

	public void writeToNBT(NBTTagCompound nbt, String key) {
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < size; i++) {
			NBTTagCompound cpd = new NBTTagCompound();
			items[i].writeToNBT(cpd);
			itemList.appendTag(cpd);
		}
		nbt.setTag(key, itemList);
	}

	@Override
	public int getSizeInventory() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		// TODO 1.11
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return items[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (!this.items[index].isEmpty()) {
			if (this.items[index].getCount() <= count) {
				ItemStack stack = this.items[index];
				this.items[index] = ItemStack.EMPTY;

				this.markDirty();
				return stack;
			} else {
				ItemStack stack = this.items[index].splitStack(count);

				if (this.items[index].getCount() <= 0) {
					this.items[index] = ItemStack.EMPTY;
				}

				this.markDirty();
				return stack;
			}
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack stack = items[index];
		items[index] = ItemStack.EMPTY;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		items[index] = stack;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
		owner.onInventoryChanged(this);
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		watchers.add(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		watchers.remove(player);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
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
		return null;
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
