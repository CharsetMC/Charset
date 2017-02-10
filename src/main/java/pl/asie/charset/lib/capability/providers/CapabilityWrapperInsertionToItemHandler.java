package pl.asie.charset.lib.capability.providers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.capability.CapabilityHelper;

/**
 * Created by asie on 11/29/16.
 */
public class CapabilityWrapperInsertionToItemHandler implements CapabilityHelper.Wrapper<IItemInsertionHandler> {
	@Override
	public IItemInsertionHandler get(ICapabilityProvider provider, EnumFacing facing) {
		IItemHandler handler = CapabilityHelper.get(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, provider, facing);
		if (handler != null) {
			return new IItemInsertionHandler() {
				@Override
				public ItemStack insertItem(ItemStack stack, boolean simulate) {
					return ItemHandlerHelper.insertItem(handler, stack, simulate);
				}
			};
		} else {
			return null;
		}
	}
}
