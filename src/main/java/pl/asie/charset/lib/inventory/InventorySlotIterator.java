package pl.asie.charset.lib.inventory;

import java.util.Iterator;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ILockableContainer;

public final class InventorySlotIterator implements Iterator<InventorySlot> {
	public final IInventory inv;
	public final EnumFacing side;
	private final int[] sides;
	private int slot = 0;

	public InventorySlotIterator(IInventory inv, EnumFacing side) {
		this.inv = inv;
		this.side = side;
		if (inv instanceof ISidedInventory) {
			sides = ((ISidedInventory) inv).getSlotsForFace(side);
		} else {
			sides = null;
		}
	}

	@Override
	public boolean hasNext() {
		if (inv instanceof ILockableContainer && ((ILockableContainer) inv).isLocked()) {
			return false;
		}

		return slot < (sides != null ? sides.length : inv.getSizeInventory());
	}

	@Override
	public InventorySlot next() {
		InventorySlot s = new InventorySlot(inv, side, sides != null ? sides[slot] : slot);
		slot++;
		return s;
	}
}
