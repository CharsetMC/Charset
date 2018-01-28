package pl.asie.charset.module.tweak.improvedCauldron;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.loader.ModuleProfile;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.module.storage.tanks.TileTank;
import pl.asie.charset.module.storage.tanks.TileTankRenderer;
import pl.asie.charset.module.tweak.remove.netherPortals.BlockPortalBlocked;

@CharsetModule(
		name = "tweak.improvedCauldron",
		description = "Improved Cauldron!",
		profile = ModuleProfile.EXPERIMENTAL
)
public class CharsetTweakImprovedCauldron {
	public static BlockCauldronCharset blockCauldron;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		blockCauldron = new BlockCauldronCharset();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RegistryUtils.register(TileCauldronCharset.class, "improved_cauldron");
		FMLInterModComms.sendMessage("charset", "addLock", "minecraft:cauldron");
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileCauldronCharset.class, new TileRendererCauldronCharset());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onModelBake(ModelBakeEvent event) {
		IBakedModel l0 = event.getModelRegistry().getObject(new ModelResourceLocation("minecraft:cauldron#level=0"));

		if (l0 != null) {
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=1"), l0);
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=2"), l0);
			event.getModelRegistry().putObject(new ModelResourceLocation("minecraft:cauldron#level=3"), l0);
		}
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(blockCauldron.setRegistryName("minecraft:cauldron"));
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote) {
			event.getWorld().addEventListener(CharsetCauldronEventListener.INSTANCE);
		}
	}
}
