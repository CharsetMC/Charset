package pl.asie.charset.storage;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import pl.asie.charset.audio.tape.RecipeTape;
import pl.asie.charset.audio.tape.RecipeTapeReel;
import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.storage.backpack.HandlerBackpack;
import pl.asie.charset.storage.backpack.BlockBackpack;
import pl.asie.charset.storage.backpack.ItemBackpack;
import pl.asie.charset.storage.backpack.PacketBackpackOpen;
import pl.asie.charset.storage.backpack.TileBackpack;
import pl.asie.charset.storage.locking.*;

import java.util.Random;
import java.util.UUID;

@Mod(modid = ModCharsetStorage.MODID, name = ModCharsetStorage.NAME, version = ModCharsetStorage.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetStorage {
	public static final String MODID = "CharsetStorage";
	public static final String NAME = "#";
	public static final String VERSION = "@VERSION@";
	public static final int DEFAULT_LOCKING_COLOR = 0xFBDB6A;
	private static final Random rand = new Random();

	@Mod.Instance(MODID)
	public static ModCharsetStorage instance;

	@SidedProxy(clientSide = "pl.asie.charset.storage.ProxyClient", serverSide = "pl.asie.charset.storage.ProxyCommon")
	public static ProxyCommon proxy;

	public static PacketRegistry packet;
	public static Logger logger;

	public static BlockBackpack backpackBlock;
	public static ItemMasterKey masterKeyItem;
	public static ItemKey keyItem;
	public static ItemLock lockItem;

	public static boolean enableBackpackOpenKey;

	private Configuration config;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetStorage.MODID);
		config = new Configuration(ModCharsetLib.instance.getConfigFile("storage.cfg"));

		backpackBlock = new BlockBackpack();
		GameRegistry.register(backpackBlock.setRegistryName("backpack"));
		GameRegistry.register(new ItemBackpack(backpackBlock).setRegistryName("backpack"));

		masterKeyItem = new ItemMasterKey();
		GameRegistry.register(masterKeyItem.setRegistryName("masterKey"));

		keyItem = new ItemKey();
		GameRegistry.register(keyItem.setRegistryName("key"));

		lockItem = new ItemLock();
		GameRegistry.register(lockItem.setRegistryName("lock"));

		ModCharsetLib.proxy.registerItemModel(backpackBlock, 0, "charsetstorage:backpack");
		ModCharsetLib.proxy.registerItemModel(masterKeyItem, 0, "charsetstorage:masterKey");
		ModCharsetLib.proxy.registerItemModel(keyItem, 0, "charsetstorage:key");
		ModCharsetLib.proxy.registerItemModel(lockItem, 0, "charsetstorage:lock");

		MinecraftForge.EVENT_BUS.register(proxy);

		enableBackpackOpenKey = config.getBoolean("enableOpenKeyBinding", "backpack", true, "Should backpacks be openable with a key binding?");

		config.save();

		proxy.preInit();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerTileEntity(TileBackpack.class, "charset:backpack");
		EntityRegistry.registerModEntity(EntityLock.class, "charsetstorage:lock", 1, this, 64, 3, true);

		proxy.init();

		packet = new PacketRegistry(ModCharsetStorage.MODID);
		packet.registerPacket(0x01, PacketBackpackOpen.class);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerStorage());

		MinecraftForge.EVENT_BUS.register(new HandlerBackpack());
		MinecraftForge.EVENT_BUS.register(new LockEventHandler());

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(backpackBlock), "lgl", "scs", "lwl",
				'l', Items.LEATHER, 'c', "chestWood", 's', "stickWood", 'g', "ingotGold", 'w', Blocks.WOOL));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(keyItem), "ng", "ng", " g", 'n', "nuggetGold", 'g', "ingotGold") {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack result = output.copy();
				result.setTagCompound(new NBTTagCompound());
				result.getTagCompound().setString("key", new UUID(rand.nextLong(), rand.nextLong()).toString());
				return result;
			}
		});

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(keyItem), "ng", "ng", "kg", 'n', "nuggetGold", 'g', "ingotGold", 'k', keyItem) {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack key = inv.getStackInRowAndColumn(0, 2);
				if (key == null) {
					key = inv.getStackInRowAndColumn(1, 2);
				}

				if (key != null && key.getItem() instanceof ItemKey) {
					ItemStack result = output.copy();
					result.setTagCompound(new NBTTagCompound());
					result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getRawKey(key));
					return result;
				}
				return null;
			}
		});

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lockItem), " g ", "gkg", "gig", 'i', "ingotIron", 'g', "ingotGold", 'k', keyItem) {
			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack key = inv.getStackInRowAndColumn(1, 1);
				if (key != null && key.getItem() instanceof ItemKey) {
					ItemStack result = output.copy();
					result.setTagCompound(new NBTTagCompound());
					result.getTagCompound().setString("key", ((ItemKey) key.getItem()).getRawKey(key));
					return result;
				}
				return null;
			}
		});

		GameRegistry.addRecipe(new RecipeDyeLock());
		RecipeSorter.register("charsetstorage:lockDye", RecipeDyeLock.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
	}
}
