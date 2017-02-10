package pl.asie.charset.storage.barrels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import pl.asie.charset.lib.annotation.CharsetModule;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;

@CharsetModule(
		name = "storage.barrels",
		description = "Simple barrels"
)
public class CharsetStorageBarrels {
	@CharsetModule.Instance
	public static CharsetStorageBarrels instance;

	@CharsetModule.PacketRegistry
	public static PacketRegistry packet;

	@CharsetModule.Configuration
	public static Configuration config;

	public static BlockBarrel barrelBlock;
	public static ItemDayBarrel barrelItem;
	public static ItemMinecartDayBarrel barrelCartItem;

	public static boolean renderBarrelText, renderBarrelItem, renderBarrelItem3D;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		barrelBlock = new BlockBarrel();
		barrelItem = new ItemDayBarrel(barrelBlock);
		barrelCartItem = new ItemMinecartDayBarrel();
		RegistryUtils.register(barrelBlock, barrelItem, "barrel");
		RegistryUtils.register(barrelCartItem, "barrelCart");

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new BarrelEventListener());

		RegistryUtils.registerModel(barrelItem, 0, "charset:barrel");
		RegistryUtils.registerModel(barrelCartItem, 0, "charset:barrelCart");

		renderBarrelItem3D = config.getBoolean("renderItem3D", "barrels", false, "Should items use fancy 3D rendering?");
		renderBarrelItem = config.getBoolean("renderItem", "barrels", true, "Should items be rendered on barrels?");
		renderBarrelText = config.getBoolean("renderText", "barrels", true, "Should text be rendered on barrels?");

		FMLInterModComms.sendMessage("charset", "addCarry", barrelBlock.getRegistryName());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerTileEntityWithAlternatives(TileEntityDayBarrel.class, "charset:barrel", "charsetstorage:barrel");
		RegistryUtils.register(EntityMinecartDayBarrel.class, "barrelCart", 2, 64, 1, true);

		packet.registerPacket(0x01, PacketBarrelCountUpdate.class);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// TODO
		BarrelRegistry.INSTANCE.register(TileEntityDayBarrel.Type.CREATIVE,
				ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.BEDROCK)),
				ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.DIAMOND_BLOCK))
		);
		barrelCartItem.setMaxStackSize(new ItemStack(Items.CHEST_MINECART).getMaxStackSize()); // Railcraft compat

		GameRegistry.addRecipe(new BarrelCartRecipe());
		BarrelUpgradeRecipes.addUpgradeRecipes();

		RecipeSorter.register("charsetstorage:barrelCart", BarrelCartRecipe.class, RecipeSorter.Category.SHAPELESS, "");

		// If you stop this from happening in postInit, please adjust TextureStitchEvent in ProxyClient
		for (ItemMaterial log : ItemMaterialRegistry.INSTANCE.getMaterialsByTypes("log", "block")) {
			ItemMaterial plank = log.getRelated("plank");
			if (plank != null) {
				ItemMaterial slab = plank.getRelated("slab");
				if (slab == null) {
					slab = plank;
				}
				BarrelRegistry.INSTANCE.registerCraftable(log, slab);
			}
		}
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityMinecartDayBarrel.class, manager -> new RenderMinecartDayBarrel(manager));
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDayBarrel.class, new TileEntityDayBarrelRenderer());

		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(BarrelModel.INSTANCE.colorizer, barrelBlock);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(BarrelModel.INSTANCE.colorizer, barrelItem);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemMinecartDayBarrel.Color(), barrelCartItem);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureMap(TextureStitchEvent.Pre event) {
		BarrelModel.INSTANCE.onTextureLoad(event.getMap());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPostBake(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:barrel", "normal"), BarrelModel.INSTANCE);
		event.getModelRegistry().putObject(new ModelResourceLocation("charset:barrel", "inventory"), BarrelModel.INSTANCE);

		BarrelModel.INSTANCE.template = (IRetexturableModel) RenderUtils.getModel(new ResourceLocation("charset:block/barrel"));
	}
}
