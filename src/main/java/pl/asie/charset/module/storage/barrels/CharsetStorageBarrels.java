package pl.asie.charset.module.storage.barrels;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
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
import pl.asie.charset.ModCharset;
import pl.asie.charset.lib.loader.CharsetModule;
import pl.asie.charset.lib.material.ItemMaterial;
import pl.asie.charset.lib.material.ItemMaterialRegistry;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.lib.utils.RegistryUtils;
import pl.asie.charset.lib.utils.RenderUtils;
import pl.asie.charset.module.misc.scaffold.ModelScaffold;

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
	public static boolean enableSilkyBarrels, enableHoppingBarrels;

	public static boolean isEnabled(TileEntityDayBarrel.Type type) {
		if (type == TileEntityDayBarrel.Type.SILKY) {
			return enableSilkyBarrels;
		} else if (type == TileEntityDayBarrel.Type.HOPPING) {
			return enableHoppingBarrels;
		} else if (type == TileEntityDayBarrel.Type.LARGER) {
			return false;
		} else {
			return true;
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		barrelBlock = new BlockBarrel();
		barrelItem = new ItemDayBarrel(barrelBlock);
		barrelCartItem = new ItemMinecartDayBarrel();

		MinecraftForge.EVENT_BUS.register(new BarrelEventListener());

		renderBarrelItem3D = config.getBoolean("renderItem3D", "render", false, "Should items use fancy 3D rendering?");
		renderBarrelItem = config.getBoolean("renderItem", "render", true, "Should items be rendered on barrels?");
		renderBarrelText = config.getBoolean("renderText", "render", true, "Should text be rendered on barrels?");
		enableSilkyBarrels = config.getBoolean("enableSilkyBarrels", "features", !ModCharset.isModuleLoaded("tweak.blockCarrying"), "Enable silky barrels. On by default unless tweak.blockCarrying is also present.");
		enableHoppingBarrels = config.getBoolean("enableHoppingBarrels", "features", true, "Enable hopping barrels. On by default.");
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		RegistryUtils.registerModel(barrelItem, 0, "charset:barrel");
		RegistryUtils.registerModel(barrelCartItem, 0, "charset:barrelCart");
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		RegistryUtils.register(event.getRegistry(), barrelBlock, "barrel");
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		RegistryUtils.register(event.getRegistry(), barrelItem, "barrel");
		RegistryUtils.register(event.getRegistry(), barrelCartItem, "barrelCart");
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerTileEntity(TileEntityDayBarrel.class, "charset:barrel");
		RegistryUtils.register(EntityMinecartDayBarrel.class, "barrelCart", 64, 1, true);

		packet.registerPacket(0x01, PacketBarrelCountUpdate.class);
		FMLInterModComms.sendMessage("charset", "addCarry", barrelBlock.getRegistryName());
	}

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		BarrelRegistry.INSTANCE.register(TileEntityDayBarrel.Type.CREATIVE,
				ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.BEDROCK)),
				ItemMaterialRegistry.INSTANCE.getOrCreateMaterial(new ItemStack(Blocks.DIAMOND_BLOCK))
		);

		event.getRegistry().register(new BarrelCartRecipe("barrel_cart").setRegistryName("barrel_cart"));
		BarrelUpgradeRecipes.addUpgradeRecipes(event.getRegistry());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		barrelCartItem.setMaxStackSize(new ItemStack(Items.CHEST_MINECART).getMaxStackSize()); // Railcraft compat

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
		RenderingRegistry.registerEntityRenderingHandler(EntityMinecartDayBarrel.class, RenderMinecartDayBarrel::new);
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

		BarrelModel.INSTANCE.template = RenderUtils.getModel(new ResourceLocation("charset:block/barrel"));
	}
}
