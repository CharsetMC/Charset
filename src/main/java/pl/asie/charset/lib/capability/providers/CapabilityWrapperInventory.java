package pl.asie.charset.lib.capability.providers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import pl.asie.charset.lib.capability.CapabilityHelper;

/**
 * Created by asie on 11/29/16.
 */
public class CapabilityWrapperInventory implements CapabilityHelper.Wrapper<IItemHandler> {
	@Override
	public IItemHandler get(ICapabilityProvider provider, EnumFacing facing) {
		if (provider instanceof ISidedInventory) {
			IItemHandler handler = new SidedInvWrapper((ISidedInventory) provider, facing);
			return handler.getSlots() > 0 ? handler : null;
		} else if (provider instanceof IInventory) {
			return new InvWrapper((IInventory) provider);
		} else {
			return null;
		}
	}
}
