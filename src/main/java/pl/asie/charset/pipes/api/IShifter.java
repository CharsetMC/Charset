package pl.asie.charset.pipes.api;

import net.minecraft.item.ItemStack;

import net.minecraft.util.EnumFacing;

public interface IShifter {
	enum Mode {
		Extract,
		Push
	}

	Mode getMode();
	EnumFacing getDirection();
	int getShiftDistance();
	boolean isShifting();
	boolean hasFilter();
	boolean matches(ItemStack source);
}
