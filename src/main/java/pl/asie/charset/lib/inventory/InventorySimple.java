package pl.asie.charset.lib.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;

/**
 * Created by asie on 1/10/16.
 */
public class InventorySimple implements IInventory {
	private final IInventoryOwner owner;
	private final int size;
	private final ItemStack[] items;

	public InventorySimple(int size, IInventoryOwner owner) {
		this.owner = owner;
		this.size = size;
		this.items = new ItemStack[size];
	}

	public void readFromNBT(NBTTagCompound nbt, String key) {
		NBTTagList itemList = nbt.getTagList(key, 10);
		for (int i = 0; i < Math.min(size, itemList.tagCount()); i++) {
			NBTTagCompound cpd = itemList.getCompoundTagAt(i);
			ItemStack stack = ItemStack.loadItemStackFromNBT(cpd);
			if (stack != null) {
				items[i] = stack;
			} else {
				items[i] = null;
			}
		}
	}

	public void writeToNBT(NBTTagCompound nbt, String key) {
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < size; i++) {
			NBTTagCompound cpd = new NBTTagCompound();
			if (items[i] != null) {
				items[i].writeToNBT(cpd);
			}
			itemList.appendTag(cpd);
		}
		nbt.setTag(key, itemList);
	}

	@Override
	public int getSizeInventory() {
		return size;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return items[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (this.items[index] != null) {
			if (this.items[index].stackSize <= count) {
				ItemStack stack = this.items[index];
				this.items[index] = null;

				this.markDirty();
				return stack;
			} else {
				ItemStack stack = this.items[index].splitStack(count);

				if (this.items[index].stackSize == 0) {
					this.items[index] = null;
				}

				this.markDirty();
				return stack;
			}
		} else {
			return null;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack stack = items[index];
		items[index] = null;
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
	public boolean isUseableByPlayer(EntityPlayer player) {
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
	public IChatComponent getDisplayName() {
		return null;
	}
}
