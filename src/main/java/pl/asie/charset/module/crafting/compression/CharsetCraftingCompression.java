package pl.asie.charset.module.crafting.compression;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.item.ItemBlockBase;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.UtilProxyCommon;

@CharsetModule(
		name = "crafting.compression",
		description = "Compression Crafter, for all your large-scale auto-crafting needs.",
		dependencies = {"storage.barrels"},
		profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetCraftingCompression {
	public static BlockCompressionCrafter blockCompressionCrafter;
	public static ItemBlock itemCompressionCrafter;

	@CharsetModule.SidedProxy(clientSide = "pl.asie.charset.module.crafting.compression.ProxyClient", serverSide = "pl.asie.charset.module.crafting.compression.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		blockCompressionCrafter = new BlockCompressionCrafter();
		itemCompressionCrafter = new ItemBlockBase(blockCompressionCrafter);

		MinecraftForge.EVENT_BUS.register(proxy);
	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		RegistryUtils.register(TileCompressionCrafter.class, "compression_crafter");
	}

	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), blockCompressionCrafter, "compression_crafter");
	}

	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), itemCompressionCrafter, "compression_crafter");
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelRegistry(ModelRegistryEvent event) {
		RegistryUtils.registerModel(itemCompressionCrafter, 0, "charset:compression_crafter");
	}
}
