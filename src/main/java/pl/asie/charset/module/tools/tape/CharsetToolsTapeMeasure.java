package pl.asie.charset.module.tools.tape;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;

/* @CharsetModule(
		name = "tools.tapemeasure",
		description = "Tape Measure, for measuring distances",
		profile = ModuleProfile.INDEV
) */
public class CharsetToolsTapeMeasure {
	public static ItemTapeMeasure tapeMeasure;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		tapeMeasure = new ItemTapeMeasure();
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void onPreInitClient(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(TapeMeasureRenderer.INSTANCE);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelRegister(ModelRegistryEvent event) {
		RegistryUtils.registerModel(tapeMeasure, 0, "charset:tape_measure");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), tapeMeasure, "tape_measure");
	}
}
