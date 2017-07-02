package pl.asie.charset.lib.modcompat.baubles;

import baubles.api.cap.IBaublesItemHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import pl.asie.charset.api.lib.EntityGatherItemsEvent;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;

@CharsetModule(
	name = "baubles:lib",
	dependencies = {"mod:baubles"},
	profile = ModuleProfile.COMPAT
)
public class CharsetBaublesCompat {
	@CapabilityInject(IBaublesItemHandler.class)
	public static Capability baublesItemHandler;
	@CharsetModule.Instance
	public static CharsetBaublesCompat instance;

	@Mod.EventHandler
	public void register(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onGatherItems(EntityGatherItemsEvent event) {
		if (event.collectsWorn()) {
			if (baublesItemHandler != null && event.getEntity().hasCapability(baublesItemHandler, null)) {
				IItemHandler handler = (IItemHandler) event.getEntity().getCapability(baublesItemHandler, null);
				for (int i = 0; i < handler.getSlots(); i++) {
					event.addStack(handler.getStackInSlot(i));
				}
			}
		}
	}
}
