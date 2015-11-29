package pl.asie.charset.lib.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;

public class InventorySlot {
	public final ForgeDirection side;
	public final int slot;

	private final IInventory inventory;
	private final ISidedInventory sidedInventory;

	public InventorySlot(IInventory inv, ForgeDirection side, int slot) {
		if (inv instanceof ISidedInventory) {
			sidedInventory = (ISidedInventory) inv;
		} else {
			sidedInventory = null;
		}
		this.inventory = inv;
		this.side = side;
		this.slot = slot;
	}

	public ItemStack get() {
		return inventory.getStackInSlot(slot);
	}

	public boolean set(@Nonnull ItemStack stack, boolean simulate) {
		if (!inventory.isItemValidForSlot(slot, stack)) {
			return false;
		}
		if (sidedInventory != null && !sidedInventory.canInsertItem(slot, stack, side.ordinal())) {
			return false;
		}
		if (!simulate) {
			inventory.setInventorySlotContents(slot, stack);
			inventory.markDirty();
		}
		return true;
	}

	public int add(@Nonnull ItemStack added, boolean simulate) {
		ItemStack stack = get();
		if (stack == null || stack.stackSize == 0) {
			return set(added.copy(), simulate) ? added.stackSize : 0;
		}

		if (added.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(added, stack)) {
			int originalSize = stack.stackSize;
			stack = stack.copy();

			stack.stackSize += added.stackSize;
			stack.stackSize = Math.min(stack.stackSize, Math.min(stack.getMaxStackSize(), inventory.getInventoryStackLimit()));

			if (sidedInventory != null && !sidedInventory.canInsertItem(slot, stack, side.ordinal())) {
				return 0;
			} else if (!inventory.isItemValidForSlot(slot, stack)) {
				return 0;
			} else {
				if (!simulate) {
					inventory.setInventorySlotContents(slot, stack);
					inventory.markDirty();
				}
				return stack.stackSize - originalSize;
			}
		} else {
			return 0;
		}
	}

	public ItemStack remove(int count, boolean simulate) {
		ItemStack stack = get();
		ItemStack extracted;

		if (stack == null || stack.stackSize == 0) {
			return null;
		} else if (stack.stackSize > count) {
			extracted = stack.copy();
			extracted.stackSize = count;
		} else {
			extracted = stack;
		}

		if (sidedInventory != null) {
			if (!sidedInventory.canExtractItem(slot, extracted, side.ordinal())) {
				return null;
			}
		}

		if (!simulate) {
			if (stack.stackSize > count) {
				ItemStack result = inventory.decrStackSize(slot, count);
				inventory.markDirty();
				return result;
			} else {
				inventory.setInventorySlotContents(slot, null);
				inventory.markDirty();
			}
		}

		return extracted;
	}
}
