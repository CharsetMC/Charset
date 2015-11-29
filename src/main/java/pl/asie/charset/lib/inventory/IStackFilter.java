package pl.asie.charset.lib.inventory;

import net.minecraft.item.ItemStack;

public interface IStackFilter {
	boolean matches(ItemStack stack);
}
