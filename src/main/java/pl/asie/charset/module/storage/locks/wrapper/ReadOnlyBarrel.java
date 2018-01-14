package pl.asie.charset.module.storage.locks.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.storage.IBarrel;

public class ReadOnlyBarrel implements IBarrel {
	private final IBarrel parent;

	public ReadOnlyBarrel(IBarrel parent) {
		this.parent = parent;
	}

	@Override
	public int getItemCount() {
		return parent.getItemCount();
	}

	@Override
	public int getMaxItemCount() {
		return parent.getMaxItemCount();
	}

	@Override
	public boolean containsUpgrade(String upgradeName) {
		return parent.containsUpgrade(upgradeName);
	}

	@Override
	public boolean shouldExtractFromSide(EnumFacing side) {
		return parent.shouldExtractFromSide(side);
	}

	@Override
	public boolean shouldInsertToSide(EnumFacing side) {
		return parent.shouldInsertToSide(side);
	}

	@Override
	public ItemStack extractItem(int maxCount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(ItemStack stack, boolean simulate) {
		return stack;
	}
}
