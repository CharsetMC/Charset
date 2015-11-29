package pl.asie.charset.lib.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public final class InventoryUtils {
	public static final IStackFilter EMPTY_SLOT = new IStackFilter() {
		@Override
		public boolean matches(ItemStack stack) {
			return stack == null || stack.stackSize == 0;
		}
	};

	public static final IStackFilter NON_EMPTY_SLOT = new IStackFilter() {
		@Override
		public boolean matches(ItemStack stack) {
			return stack != null && stack.stackSize > 0;
		}
	};

	private InventoryUtils() {

	}

	public static boolean connects(IInventory inv, EnumFacing side) {
		if (inv instanceof ISidedInventory) {
			int[] slots = ((ISidedInventory) inv).getSlotsForFace(side);
			return slots != null && slots.length > 0;
		} else {
			return true;
		}
	}

	public static List<InventorySlot> getSlots(IInventory inv, EnumFacing side, IStackFilter filter) {
		ArrayList<InventorySlot> slots = new ArrayList<InventorySlot>();
		InventorySlotIterator iterator = new InventorySlotIterator(inv, side);
		while (iterator.hasNext()) {
			InventorySlot slot = iterator.next();
			if (filter.matches(slot.get())) {
				slots.add(slot);
			}
		}
		return slots;
	}

	public static InventorySlot getSlot(IInventory inv, EnumFacing side, IStackFilter filter) {
		InventorySlotIterator iterator = new InventorySlotIterator(inv, side);
		while (iterator.hasNext()) {
			InventorySlot slot = iterator.next();
			if (filter.matches(slot.get())) {
				return slot;
			}
		}
		return null;
	}

	public static int addStack(IInventory inv, EnumFacing side, ItemStack stack, boolean simulate) {
		ItemStack toAdd = stack.copy();
		InventorySlotIterator iterator = new InventorySlotIterator(inv, side);
		while (iterator.hasNext() && toAdd.stackSize > 0) {
			InventorySlot slot = iterator.next();
			toAdd.stackSize -= slot.add(toAdd, simulate);
			if (toAdd.stackSize == 0) {
				return stack.stackSize;
			}
		}
		return stack.stackSize - toAdd.stackSize;
	}
}
