package pl.asie.charset.lib.modcompat.commoncapabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;
import pl.asie.charset.lib.loader.ModCompatProvider;

public class CapabilityWrapperSlotlessInsertion implements CapabilityHelper.Wrapper<IItemInsertionHandler> {
	@CapabilityInject(ISlotlessItemHandler.class)
	public static Capability<ISlotlessItemHandler> CAP;

	@ModCompatProvider("commoncapabilities")
	public static void register() {
		CapabilityHelper.registerWrapper(Capabilities.ITEM_INSERTION_HANDLER, new CapabilityWrapperSlotlessInsertion());
	}

	@Override
	public IItemInsertionHandler get(ICapabilityProvider provider, EnumFacing side) {
		ISlotlessItemHandler handler = CapabilityHelper.get(CAP, provider, side);
		if (handler != null) {
			return new IItemInsertionHandler() {
				@Override
				public ItemStack insertItem(ItemStack stack, boolean simulate) {
					return handler.insertItem(stack, simulate);
				}
			};
		} else {
			return null;
		}
	}
}
