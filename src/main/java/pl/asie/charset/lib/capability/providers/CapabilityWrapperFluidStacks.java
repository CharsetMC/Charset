package pl.asie.charset.lib.capability.providers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import pl.asie.charset.lib.capability.CapabilityHelper;

/**
 * Created by asie on 11/29/16.
 */
public class CapabilityWrapperFluidStacks implements CapabilityHelper.Wrapper<IFluidHandler> {
	@Override
	public IFluidHandler get(ICapabilityProvider provider, EnumFacing side) {
		if (provider instanceof ItemStack && provider.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, side)) {
			return provider.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, side);
		} else {
			return null;
		}
	}
}
