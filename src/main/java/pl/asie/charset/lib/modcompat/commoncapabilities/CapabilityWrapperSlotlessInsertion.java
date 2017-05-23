package pl.asie.charset.lib.modcompat.commoncapabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import pl.asie.charset.api.lib.IItemInsertionHandler;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.capability.Capabilities;
import pl.asie.charset.lib.capability.CapabilityHelper;

@CharsetModule(
	name = "commoncapabilities:lib.slotlessInsertion",
	isModCompat = true,
	dependencies = {"mod:commoncapabilities"}
)
public class CapabilityWrapperSlotlessInsertion implements CapabilityHelper.Wrapper<IItemInsertionHandler> {
	@CapabilityInject(ISlotlessItemHandler.class)
	public static Capability<ISlotlessItemHandler> CAP;
	@CharsetModule.Instance
	public static CapabilityWrapperSlotlessInsertion instance;

	@Mod.EventHandler
	public void register(FMLPostInitializationEvent event) {
		CapabilityHelper.registerWrapper(Capabilities.ITEM_INSERTION_HANDLER, instance);
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
