package pl.asie.charset.lib.inventory;

import java.util.Iterator;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraftforge.common.util.ForgeDirection;

public final class InventoryIterator implements Iterator<InventorySlot> {
	public final IInventory inv;
	public final ForgeDirection side;
	private final int[] sides;
	private int slot = 0;

	public InventoryIterator(IInventory inv, ForgeDirection side) {
		this.inv = inv;
		this.side = side;
		if (inv instanceof ISidedInventory) {
			sides = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side.ordinal());
		} else {
			sides = null;
		}
	}

	@Override
	public boolean hasNext() {
		return slot < (sides != null ? sides.length : inv.getSizeInventory());
	}

	@Override
	public InventorySlot next() {
		InventorySlot s = new InventorySlot(inv, side, sides != null ? sides[slot] : slot);
		slot++;
		return s;
	}
}
