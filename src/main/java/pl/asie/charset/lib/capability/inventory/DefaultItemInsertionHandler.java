package pl.asie.charset.lib.capability.inventory;

import net.minecraft.item.ItemStack;
import pl.asie.charset.api.lib.IItemInsertionHandler;

public class DefaultItemInsertionHandler implements IItemInsertionHandler {
	@Override
	public ItemStack insertItem(ItemStack stack, boolean simulate) {
		return stack;
	}
}
