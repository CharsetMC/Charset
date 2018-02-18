package pl.asie.charset.lib.modcompat.buildcraft;

import buildcraft.api.transport.IInjectable;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.lib.IItemInsertionHandler;

public class ItemInsertionHandlerBuildCraft implements IItemInsertionHandler {
	private final IInjectable parent;
	private final EnumFacing side;

	public ItemInsertionHandlerBuildCraft(IInjectable parent, EnumFacing side) {
		this.parent = parent;
		this.side = side;
	}

	@Override
	public ItemStack insertItem(ItemStack stack, boolean simulate) {
		return insertItem(stack, null, simulate);
	}

	@Override
	public ItemStack insertItem(ItemStack stack, EnumDyeColor color, boolean simulate) {
		return parent.injectItem(stack, !simulate, side, color, 0);
	}
}
