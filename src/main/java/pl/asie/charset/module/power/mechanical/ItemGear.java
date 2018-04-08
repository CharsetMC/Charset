package pl.asie.charset.module.power.mechanical;

import net.minecraft.item.ItemStack;
import pl.asie.charset.lib.item.ItemBase;
import pl.asie.charset.module.power.mechanical.api.IItemGear;

public class ItemGear extends ItemBase implements IItemGear {
	private final int value;

	public ItemGear(int value) {
		super();
		this.value = value;
	}

	@Override
	public int getGearValue(ItemStack stack) {
		return value;
	}
}
