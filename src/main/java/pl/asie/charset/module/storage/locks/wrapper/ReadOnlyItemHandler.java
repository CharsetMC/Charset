package pl.asie.charset.module.storage.locks.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ReadOnlyItemHandler implements IItemHandler {
	private final IItemHandler parent;

	public ReadOnlyItemHandler(IItemHandler parent) {
		this.parent = parent;
	}

	@Override
	public int getSlots() {
		return parent.getSlots();
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return parent.getStackInSlot(slot);
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		return stack;
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return parent.getSlotLimit(slot);
	}
}
