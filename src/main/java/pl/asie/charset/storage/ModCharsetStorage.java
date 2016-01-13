package pl.asie.charset.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

import pl.asie.charset.lib.ModCharsetLib;
import pl.asie.charset.lib.network.PacketRegistry;
import pl.asie.charset.storage.backpack.HandlerBackpackUnequip;
import pl.asie.charset.storage.backpack.BlockBackpack;
import pl.asie.charset.storage.backpack.ItemBackpack;
import pl.asie.charset.storage.backpack.PacketBackpackOpen;
import pl.asie.charset.storage.backpack.TileBackpack;

@Mod(modid = ModCharsetStorage.MODID, name = ModCharsetStorage.NAME, version = ModCharsetStorage.VERSION,
		dependencies = ModCharsetLib.DEP_NO_MCMP, updateJSON = ModCharsetLib.UPDATE_URL)
public class ModCharsetStorage {
	public static final String MODID = "CharsetStorage";
	public static final String NAME = "☒";
	public static final String VERSION = "@VERSION@";

    @Mod.Instance(MODID)
    public static ModCharsetStorage instance;

    @SidedProxy(clientSide = "pl.asie.charset.storage.ProxyClient", serverSide = "pl.asie.charset.storage.ProxyCommon")
    public static ProxyCommon proxy;

    public static PacketRegistry packet;
	public static Logger logger;

    public static BlockBackpack backpackBlock;

    public static boolean enableBackpackOpenKey;

    private Configuration config;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(ModCharsetStorage.MODID);
        config = new Configuration(ModCharsetLib.instance.getConfigFile("storage.cfg"));

        backpackBlock = new BlockBackpack();
        GameRegistry.registerBlock(backpackBlock, ItemBackpack.class, "backpack");

        ModCharsetLib.proxy.registerItemModel(backpackBlock, 0, "charsetstorage:backpack");

        MinecraftForge.EVENT_BUS.register(proxy);
        MinecraftForge.EVENT_BUS.register(new HandlerBackpackUnequip());

        enableBackpackOpenKey = config.getBoolean("enableOpenKeyBinding", "backpack", true, "Should backpacks be openable with a key binding?");

        config.save();
	}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerTileEntity(TileBackpack.class, "charset:backpack");

        proxy.init();

        packet = new PacketRegistry(ModCharsetStorage.MODID);
        packet.registerPacket(0x01, PacketBackpackOpen.class);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerStorage());

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(backpackBlock), "lgl", "scs", "lll",
                'l', Items.leather, 'c', "chestWood", 's', "stickWood", 'g', "ingotGold"));
    }
}
